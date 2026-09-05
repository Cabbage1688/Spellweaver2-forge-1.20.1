package net.zhenhuojun.spellweaver.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zhenhuojun.spellweaver.entity.impl.MagicStarEntity;

import java.util.Map;

/**
 * 巡逻闲逛行为：以巡逻中心为中心，在32×32范围内随机闲逛。
 * 当存在攻击目标时自动停止，让追击行为接管。
 */
public class PatrolStrollBehavior extends Behavior<MagicStarEntity> {
    // 32×32范围，半边长16
    private static final int PATROL_HALF = 16;
    private int strollCooldown;

    public PatrolStrollBehavior() {
        super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MagicStarEntity entity) {
        return entity.getPatrolCenter() != null;
    }

    @Override
    protected void start(ServerLevel level, MagicStarEntity entity, long gameTime) {
        pickNewStrollTarget(entity);
    }

    @Override
    protected void tick(ServerLevel level, MagicStarEntity entity, long gameTime) {
        if (strollCooldown > 0) {
            strollCooldown--;
            if (strollCooldown <= 0) {
                pickNewStrollTarget(entity);
            }
        }
    }

    private void pickNewStrollTarget(MagicStarEntity entity) {
        BlockPos center = entity.getPatrolCenter();
        if (center == null) return;

        //计算巡逻中心与地面/水面的距离
        Vec3 vec3=new Vec3(center.getX(),center.getY(),center.getZ());
        BlockHitResult hitResult = entity.level().clip(new ClipContext(
                vec3,
                vec3.subtract(0, 16, 0),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.ANY,
                entity
        ));
        double distance=16;
        if (hitResult.getType() == HitResult.Type.BLOCK) {
             distance = vec3.y - hitResult.getLocation().y;
        }

        int x = center.getX() + entity.getRandom().nextInt(PATROL_HALF * 2 + 1) - PATROL_HALF;
        int y =distance>5? center.getY() + entity.getRandom().nextInt(7) - 3:center.getY() + entity.getRandom().nextInt(4)+2;
        int z = center.getZ() + entity.getRandom().nextInt(PATROL_HALF * 2 + 1) - PATROL_HALF;
        entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                new WalkTarget(new BlockPos(x, y, z), FollowOwnerBehavior.WALK_SPEED, 1));
        // 2~5秒后选下一个巡逻点
        strollCooldown = 40 + entity.getRandom().nextInt(60);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MagicStarEntity entity, long gameTime) {
        // 有目标时停止巡逻，让追击行为接管
        return !entity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
    }
}
