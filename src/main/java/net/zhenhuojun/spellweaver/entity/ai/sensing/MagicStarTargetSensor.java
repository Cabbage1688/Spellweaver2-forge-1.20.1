package net.zhenhuojun.spellweaver.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.phys.AABB;
import net.zhenhuojun.spellweaver.entity.impl.MagicStarEntity;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * 魔法之星目标获取传感器：
 * - 16x16x16范围内扫描，MobCategory.MONSTER类别
 * - 优先级：攻击主人者 > 主人攻击者 > 敌对生物
 * - 锁定目标后不受优先级影响，直到目标失效
 * - 扫描频率20tick（同循声守卫）
 * - 待机模式下不获取目标
 */
public class MagicStarTargetSensor extends Sensor<MagicStarEntity> {
    private static final int SCAN_HALF = 8; // 半边长8，即16x16范围
    private static final double LOSE_TARGET_DISTANCE = 32.0;
    private static final int ATTACKER_MEMORY_TICKS = 100; // 5秒内受伤记录有效
    private static final int LOSE_SIGHT_TICKS = 60; // 视线被遮挡60tick后丢失目标

    public MagicStarTargetSensor() {
        super(20); // 每20tick扫描一次，同循声守卫
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected void doTick(ServerLevel level, MagicStarEntity entity) {
        Brain<MagicStarEntity> brain = entity.getBrain();

        // 待机模式不获取目标
        if (entity.getActMode() == MagicStarEntity.ActMode.SLEEP) {
            brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
            return;
        }

        // 检查当前目标是否仍然有效（锁定后不受优先级影响）
        Optional<LivingEntity> currentTarget = brain.getMemory(MemoryModuleType.ATTACK_TARGET);
        if (currentTarget.isPresent()) {
            LivingEntity target = currentTarget.get();
            if (!isValidTarget(entity, target)) {
                // 目标失效（死亡/超出距离），清除
                brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
                entity.setLastTargetVisibleGameTime(0);
            } else {
                // 视线遮挡检测
                boolean canSee = entity.hasLineOfSight(target);
                long gameTime = level.getGameTime();
                if (canSee) {
                    entity.setLastTargetVisibleGameTime(gameTime);
                } else if (entity.getLastTargetVisibleGameTime() > 0
                        && gameTime - entity.getLastTargetVisibleGameTime() > LOSE_SIGHT_TICKS) {
                    // 视线被遮挡超过60tick，丢失目标
                    brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
                    entity.setLastTargetVisibleGameTime(0);
                    return;
                }
                return; // 保持当前目标
            }
        }

        // 按优先级寻找新目标
        LivingEntity newTarget = findTarget(level, entity);
        if (newTarget != null) {
            brain.setMemory(MemoryModuleType.ATTACK_TARGET, newTarget);
            entity.setLastTargetVisibleGameTime(level.getGameTime());
        } else {
            brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }
    }

    private boolean isValidTarget(MagicStarEntity entity, LivingEntity target) {
        if (target == null || !target.isAlive()) return false;
        if (target.level() != entity.level()) return false;
        // 超出追踪距离则丢失目标
        if (entity.distanceToSqr(target) > LOSE_TARGET_DISTANCE * LOSE_TARGET_DISTANCE) return false;
        return true;
    }

    private LivingEntity findTarget(ServerLevel level, MagicStarEntity entity) {
        ServerPlayer owner = entity.getOwner();
        AABB scanBox = new AABB(
                entity.getX() - SCAN_HALF, entity.getY() - SCAN_HALF, entity.getZ() - SCAN_HALF,
                entity.getX() + SCAN_HALF, entity.getY() + SCAN_HALF, entity.getZ() + SCAN_HALF
        );

        //攻击主人的生物（排除同主人的魔法之星，避免法术误伤主人后起内讧）
        if (owner != null && owner.isAlive()) {
            LivingEntity ownerAttacker = owner.getLastHurtByMob();
            int timestamp = owner.getLastHurtByMobTimestamp();
            if (ownerAttacker != null && ownerAttacker.isAlive()
                    && scanBox.contains(ownerAttacker.position())
                    && owner.tickCount - timestamp < ATTACKER_MEMORY_TICKS
                    && ownerAttacker != entity
                    && !isSameOwnerMagicStar(ownerAttacker, entity)
                    && entity.hasLineOfSight(ownerAttacker)) {
                return ownerAttacker;
            }
        }

        // 攻击自己的生物（排除主人和同主人的魔法之星）
        {
            LivingEntity selfAttacker = entity.getLastHurtByMob();
            int timestamp = entity.getLastHurtByMobTimestamp();
            if (selfAttacker != null && selfAttacker.isAlive()
                    && scanBox.contains(selfAttacker.position())
                    && entity.tickCount - timestamp < ATTACKER_MEMORY_TICKS
                    && selfAttacker != entity
                    && (owner == null || selfAttacker != owner)
                    && !isSameOwnerMagicStar(selfAttacker, entity)
                    && entity.hasLineOfSight(selfAttacker)) {
                return selfAttacker;
            }
        }

        // 主人攻击的生物（排除同主人的魔法之星）
        if (owner != null && owner.isAlive()) {
            LivingEntity ownerTarget = owner.getLastHurtMob();
            int timestamp = owner.getLastHurtMobTimestamp();
            if (ownerTarget != null && ownerTarget.isAlive()
                    && scanBox.contains(ownerTarget.position())
                    && owner.tickCount - timestamp < ATTACKER_MEMORY_TICKS
                    && ownerTarget != entity
                    && !isSameOwnerMagicStar(ownerTarget, entity)
                    && entity.hasLineOfSight(ownerTarget)) {
                return ownerTarget;
            }
        }

        // 扫描范围内的敌对生物（需有视线，排除同主人的魔法之星）
        List<LivingEntity> monsters = level.getEntitiesOfClass(LivingEntity.class, scanBox,
                e -> isMonster(e) && e.isAlive() && e != entity && (owner == null || e != owner)
                        && !isSameOwnerMagicStar(e, entity)
                        && entity.hasLineOfSight(e));
        if (!monsters.isEmpty()) {
            monsters.sort(Comparator.comparingDouble(entity::distanceToSqr));
            return monsters.get(0);
        }

        return null;
    }

    private boolean isMonster(Entity entity) {
        return entity.getType().getCategory() == MobCategory.MONSTER;
    }

    /**
     * 判断目标是否为同一主人的魔法之星（避免魔法之星之间互相攻击）
     */
    private boolean isSameOwnerMagicStar(LivingEntity target, MagicStarEntity self) {
        if (!(target instanceof MagicStarEntity otherStar)) return false;
        UUID selfOwner = self.getOwnerUUID();
        UUID otherOwner = otherStar.getOwnerUUID();
        return selfOwner != null && selfOwner.equals(otherOwner);
    }
}
