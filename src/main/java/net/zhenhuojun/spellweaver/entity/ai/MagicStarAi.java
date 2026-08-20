package net.zhenhuojun.spellweaver.entity.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.zhenhuojun.spellweaver.entity.ai.behavior.CastSpellListBehavior;
import net.zhenhuojun.spellweaver.entity.ai.behavior.DefensiveMeleeBehavior;
import net.zhenhuojun.spellweaver.entity.ai.behavior.FollowOwnerBehavior;
import net.zhenhuojun.spellweaver.entity.ai.behavior.PatrolStrollBehavior;
import net.zhenhuojun.spellweaver.entity.impl.MagicStarEntity;

import java.util.List;
import java.util.Map;

/**
 * 魔法之星AI核心类：
 * - 参考循声守卫的brain架构
 * - 四种模式（跟随/定点/待机/巡逻）通过Activity切换
 * - Core层包含移动执行、法术施放、防御性近战
 * - 目标获取通过MagicStarTargetSensor
 */
public class MagicStarAi {
    // 发现目标后的追击速度倍率
    private static final float CHASE_SPEED_MODIFIER = 6.0F;

    // 注册的内存模块
    private static final List<MemoryModuleType<?>> MEMORY_TYPES = List.of(
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.PATH,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE
    );

    // 注册的传感器
    private static final List<SensorType<? extends Sensor<? super MagicStarEntity>>> SENSOR_TYPES = List.of(
            ModSensors.MAGIC_STAR_TARGET.get()
    );

    public static Brain.Provider<MagicStarEntity> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    /**
     * 初始化Brain，配置各Activity的行为
     */
    public static Brain<MagicStarEntity> makeBrain(MagicStarEntity entity, Dynamic<?> dynamic) {
        Brain.Provider<MagicStarEntity> provider = brainProvider();
        Brain<MagicStarEntity> brain = provider.makeBrain(dynamic);

        initCoreActivity(brain);
        initFollowActivity(brain);
        initStopActivity(brain);
        initSleepActivity(brain);
        initPatrolActivity(brain);

        // Core行为在所有模式下运行
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        // 默认活动
        //brain.setDefaultActivity(ModActivities.FOLLOW.get());
        brain.setDefaultActivity(ModActivities.SLEEP.get());
        brain.useDefaultActivity();

        return brain;
    }

    /**
     * Core行为：移动执行、法术施放、防御性近战
     * 这些行为在所有模式下运行（待机模式下传感器不会设置ATTACK_TARGET，故攻击相关行为自动不触发）
     */
    private static void initCoreActivity(Brain<MagicStarEntity> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                // 执行WALK_TARGET的实际移动
                new MoveToTargetSink(),
                // 受伤时施放自保法术
                new CastSpellListBehavior(
                        Map.of(),
                        MagicStarEntity::getShieldSpells,
                        e -> e.isShieldCastRequested() && e.isMasterPermitShield(),
                        e -> null,
                        MagicStarEntity::clearShieldCastRequest,
                        1 // listType=1(自保)
                ),
                // 主人受伤时施放保护法术
                new CastSpellListBehavior(
                        Map.of(),
                        MagicStarEntity::getProtectMasterSpells,
                        e -> e.isProtectMasterCastRequested() && e.isMasterPermitProtectMaster(),
                        MagicStarEntity::getOwner,
                        MagicStarEntity::clearProtectMasterCastRequest,
                        2 // listType=2(护主)
                ),
                // 有目标时施放攻击法术（需ATTACK_TARGET存在）
                new CastSpellListBehavior(
                        Map.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT),
                        MagicStarEntity::getAttackSpells,
                        MagicStarEntity::isMasterPermitAttack,
                        e -> e.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null),
                        e -> {},
                        10, 20,
                        0 // listType=0(攻击)
                ),
                // 防御性近战：敌人靠近时近战
                new DefensiveMeleeBehavior(),
                // 日常法术：每20tick轮播一次，不需要目标
                new CastSpellListBehavior(
                        Map.of(),
                        MagicStarEntity::getRoutineSpells,
                        MagicStarEntity::isMasterPermitRoutine,
                        e -> null,
                        e -> {},
                        20, 20,
                        3 // listType=3(日常)
                )
        ));
    }

    /**
     * 跟随模式：无目标时跟随主人，有目标时追击
     */
    private static void initFollowActivity(Brain<MagicStarEntity> brain) {
        brain.addActivity(ModActivities.FOLLOW.get(), 10, ImmutableList.of(
                // 无目标时跟随主人（entryCondition: ATTACK_TARGET absent）
                new FollowOwnerBehavior(),
                // 有目标时追击（entryCondition: ATTACK_TARGET present）
                SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(CHASE_SPEED_MODIFIER)
        ));
    }

    /**
     * 定点模式：不移动，但仍能施放法术和近战（Core层处理）
     */
    private static void initStopActivity(Brain<MagicStarEntity> brain) {
        brain.addActivity(ModActivities.STOP.get(), 10, ImmutableList.of());
    }

    /**
     * 待机模式：不移动不获取目标（Core层的攻击行为因无ATTACK_TARGET自动不触发）
     */
    private static void initSleepActivity(Brain<MagicStarEntity> brain) {
        brain.addActivity(ModActivities.SLEEP.get(), 10, ImmutableList.of());
    }

    /**
     * 巡逻模式：无目标时在巡逻范围内闲逛，有目标时追击
     */
    private static void initPatrolActivity(Brain<MagicStarEntity> brain) {
        brain.addActivity(ModActivities.PATROL.get(), 10, ImmutableList.of(
                // 无目标时巡逻闲逛（entryCondition: ATTACK_TARGET absent）
                new PatrolStrollBehavior(),
                // 有目标时追击（entryCondition: ATTACK_TARGET present）
                SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(CHASE_SPEED_MODIFIER)
        ));
    }

    /**
     * 根据ActMode切换Activity
     * 主人下线时强制进入待机模式
     */
    public static void updateActivity(MagicStarEntity entity) {
        MagicStarEntity.ActMode mode = entity.getActMode();
        // 主人下线时强制待机
        if (entity.getOwnerUUID() != null && entity.getOwner() == null) {
            mode = MagicStarEntity.ActMode.SLEEP;
        }
        Activity activity = switch (mode) {
            case FOLLOW -> ModActivities.FOLLOW.get();
            case STOP -> ModActivities.STOP.get();
            case SLEEP -> ModActivities.SLEEP.get();
            case PATROL -> ModActivities.PATROL.get();
        };
        // 末尾添加默认FOLLOW兜底：若目标活动因任何原因无效，至少有跟随模式可用，避免Brain无有效活动导致NPE
        entity.getBrain().setActiveActivityToFirstValid(ImmutableList.of(activity, ModActivities.FOLLOW.get()));
    }
}
