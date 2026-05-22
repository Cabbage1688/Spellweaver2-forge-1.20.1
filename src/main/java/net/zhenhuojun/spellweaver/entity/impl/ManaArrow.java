package net.zhenhuojun.spellweaver.entity.impl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.zhenhuojun.spellweaver.spell.util.RunesExecuteMethod;

import java.util.ArrayList;
import java.util.List;

public class ManaArrow extends AbstractArrow {
    private List<String> spellList = new ArrayList<>();
    private int time=300;

    public ManaArrow(EntityType<? extends ManaArrow> type, Level level) {
        super(type, level);
    }

    public ManaArrow(EntityType<? extends ManaArrow> type,Level level, ItemStack bowStack) {
        //super(level, shooter);
        super(type, level);
        // 从弓的nbt中读取法术列表
        CompoundTag tag = bowStack.getTag();
        if (tag != null && tag.contains("spellList", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("spellList", Tag.TAG_STRING);
            for (int i = 0; i < listTag.size(); i++) {
                spellList.add(listTag.getString(i));
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity target) {
            // 执行法术逻辑
            if (!spellList.isEmpty() && this.getOwner() instanceof net.minecraft.world.entity.player.Player player) {
                // 重置无敌时间和伤害动画
                target.invulnerableTime = 0;
                target.hurtTime = 0;
                // 调用法术执行方法
                RunesExecuteMethod.ManaSwordSpellLogic(spellList, player.level(), player, target);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        // 如果命中其他物体，直接消失（或继续处理）
        //if (!this.level().isClientSide && result.getType() == HitResult.Type.BLOCK) {
           // this.discard();
        //}
    }

    @Override
    public void tick(){
        super.tick();
        time--;
        if(time<=0){
            this.discard();
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        // 箭矢不可拾取（因为它是魔力凝结的）
        return ItemStack.EMPTY;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // 从 NBT 恢复法术列表（用于网络同步）
        if (tag.contains("spellList", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("spellList", Tag.TAG_STRING);
            spellList.clear();
            for (int i = 0; i < listTag.size(); i++) {
                spellList.add(listTag.getString(i));
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        // 保存法术列表到 NBT
        ListTag listTag = new ListTag();
        for (String spell : spellList) {
            listTag.add(net.minecraft.nbt.StringTag.valueOf(spell));
        }
        tag.put("spellList", listTag);
    }
}