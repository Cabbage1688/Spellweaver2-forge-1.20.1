package net.zhenhuojun.spellweaver.entity.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.zhenhuojun.spellweaver.client.data_util.ClientPlayerOverloadData;

public class SpellEffectEntity extends Entity {
    private LivingEntity ownerEntity;

    public SpellEffectEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (!level.isClientSide) return; // 服务端直接不处理任何逻辑

        // 客户端专属逻辑
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            // 让实体跟随玩家（避免离得太远被卸载）
            this.setPos(player.getX(), player.getY() + 1.2, player.getZ());
        }
        // 检查状态，若已禁用则移除
        if (!ClientPlayerOverloadData.isEnabled()) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {

    }


}
