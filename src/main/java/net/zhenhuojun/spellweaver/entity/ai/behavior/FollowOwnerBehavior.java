package net.zhenhuojun.spellweaver.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import net.zhenhuojun.spellweaver.entity.impl.MagicStarEntity;

import java.util.Map;

/**
 * 跟随主人行为（仅在无攻击目标时运行）：
 * - 距离<8格：不动（但保持悬停高度）
 * - 8~16格：常速靠近
 * - 16~32格：三倍速靠近
 * - >32格：直接传送
 * - 主人脚踩地面/游泳于液面时，倾向于在2格高位置飞行，避免频繁降落
 */
public class FollowOwnerBehavior extends Behavior<MagicStarEntity> {
    private static final double FOLLOW_DISTANCE = 8.0;
    private static final double DOUBLE_SPEED_DISTANCE = 16.0;
    private static final double TELEPORT_DISTANCE = 32.0;
    private static final double HOVER_HEIGHT = 2.0;
    public static final float WALK_SPEED=2.0f;
    public static final float RUN_SPEED=6.0f;

    public FollowOwnerBehavior() {
        super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MagicStarEntity entity) {
        return entity.getOwner() != null;
    }

    @Override
    protected void start(ServerLevel level, MagicStarEntity entity, long gameTime) {
        // 无需初始化
    }

    @Override
    protected void tick(ServerLevel level, MagicStarEntity entity, long gameTime) {
        ServerPlayer owner = entity.getOwner();
        if (owner == null) return;
        double distSqr = entity.distanceToSqr(owner);

        // 主人脚踩地面/游泳于液面时，目标Y抬高2格，使魔法之星悬停飞行
        boolean shouldHover = shouldHoverAbove(owner);
        double targetY = shouldHover ? owner.getY() + HOVER_HEIGHT : owner.getY();

        if (distSqr >= TELEPORT_DISTANCE * TELEPORT_DISTANCE) {
            // 传送到主人身边
            Vec3 offset = new Vec3(
                    entity.getRandom().nextDouble() * 2 - 1,
                    0,
                    entity.getRandom().nextDouble() * 2 - 1
            );
            entity.teleportTo(owner.getX() + offset.x, targetY, owner.getZ() + offset.z);
        } else if (distSqr >= DOUBLE_SPEED_DISTANCE * DOUBLE_SPEED_DISTANCE) {
            // 三倍速
            Vec3 target = new Vec3(owner.getX(), targetY, owner.getZ());
            entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, RUN_SPEED, 1));
        } else if (distSqr >= FOLLOW_DISTANCE * FOLLOW_DISTANCE) {
            // 常速
            Vec3 target = new Vec3(owner.getX(), targetY, owner.getZ());
            entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, WALK_SPEED, 1));
        } else {
            // 8格内：需要悬停时仍设置目标以保持高度，否则不动
            if (shouldHover) {
                Vec3 target = new Vec3(owner.getX(), targetY, owner.getZ());
                entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, WALK_SPEED, 1));
            } else {
                entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            }
        }
    }

    /**
     * 判断主人是否处于需要魔法之星悬停的状态：
     * - 脚踩地面（onGround）
     * - 游泳于液体表面（在水中且正在游泳）
     */
    private boolean shouldHoverAbove(ServerPlayer owner) {
        return owner.onGround() || (owner.isInWater() && owner.isSwimming());
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MagicStarEntity entity, long gameTime) {
        return entity.getOwner() != null && !entity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
    }
}
