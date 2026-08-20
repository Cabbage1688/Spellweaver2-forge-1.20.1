package net.zhenhuojun.spellweaver.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.zhenhuojun.spellweaver.entity.impl.MagicStarEntity;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.SpreadReactionS2CPacket;

import java.util.Map;

/**
 * 防御性近战行为：
 * 实体不主动近战，但当敌人过分靠近（2格内）时会近战以防止被靠近。
 * 不设置WALK_TARGET，不会导致实体移动。
 */
public class DefensiveMeleeBehavior extends Behavior<MagicStarEntity> {
    private static final double MELEE_RANGE_SQR = 4.0; // 2格内触发近战
    private static final int ATTACK_COOLDOWN = 20; // 1秒冷却
    private int attackCooldown;

    public DefensiveMeleeBehavior() {
        super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MagicStarEntity entity) {
        LivingEntity target = entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        return target != null && entity.distanceToSqr(target) <= MELEE_RANGE_SQR;
    }

    @Override
    protected void tick(ServerLevel level, MagicStarEntity entity, long gameTime) {
        if (attackCooldown > 0) {
            attackCooldown--;
            return;
        }
        LivingEntity target = entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target != null && entity.distanceToSqr(target) <= MELEE_RANGE_SQR) {
            entity.doHurtTarget(target);
            attackCooldown = ATTACK_COOLDOWN;
            //近战特效
            ModMessage.sendToClients(new SpreadReactionS2CPacket(entity.position(),0xE9FAFF));
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MagicStarEntity entity, long gameTime) {
        return entity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
    }
}
