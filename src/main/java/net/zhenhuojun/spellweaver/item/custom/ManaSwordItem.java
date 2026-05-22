package net.zhenhuojun.spellweaver.item.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.spell.util.RunesExecuteMethod;

import java.util.ArrayList;
import java.util.List;

public class ManaSwordItem extends SwordItem {
    private static final int ATTACK_COOLDOWN_TICKS = 20;

    public ManaSwordItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }



    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {


        // 造成基础伤害
        boolean attacked = super.hurtEnemy(stack, target, attacker);
        if (attacked && attacker instanceof Player player) {
            // 设置玩家的攻击冷却
            player.getCooldowns().addCooldown(this, ATTACK_COOLDOWN_TICKS);
        }
        //法术执行
        CompoundTag tag=stack.getTag();
        if(tag!=null){
            target.invulnerableTime = 0;
            target.hurtTime = 0; //重置 hurt 动画计时
            List<String> spellList=new ArrayList<>();
            ListTag spellListTag=tag.getList("spellList", Tag.TAG_STRING);
            for(int i=0;i<spellListTag.size();i++){
                spellList.add(spellListTag.getString(i));
            }
            if(!spellList.isEmpty()){
                if(attacker instanceof Player player ){
                    RunesExecuteMethod.ManaSwordSpellLogic(spellList,player.level(),player,target);
                }
            }
        }
        //不允许破坏
        stack.setDamageValue(0);
        return attacked;
    }

   /* @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand){
        ItemStack stack = player.getItemInHand(hand);
        if(stack.is(ModItems.MANA_SWORD.get())){
            //stack.shrink(1);
            CompoundTag tag=stack.getTag();
            if(tag!=null){
                List<String> spellList=new ArrayList<>();
                ListTag spellListTag=tag.getList("spellList", Tag.TAG_STRING);
                for(int i=0;i<spellListTag.size();i++){
                    spellList.add(spellListTag.getString(i));
                }
                if(!spellList.isEmpty()){
                    if(attacker instanceof Player player ){
                        RunesExecuteMethod.ManaSwordSpellLogic(spellList,player.level(),player,target);
                    }
                }
            }


        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
    */

    public static final Tier STAFF_TIER = new Tier() {
        @Override
        public int getUses() {
            return 36760; // 耐久度，钻石剑为1561
        }

        @Override
        public float getSpeed() {
            return 4.0F; // 挖掘速度，
        }

        @Override
        public float getAttackDamageBonus() {
            return 5.0F; // 额外攻击加成
        }

        @Override
        public int getLevel() {
            return 2; // 挖掘等级，2相当于铁工具
        }

        @Override
        public int getEnchantmentValue() {
            return 0; // 附魔能力，18高于铁(14)
        }

        @Override
        public Ingredient getRepairIngredient() {
            return null;
        }

    };

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!entity.level().isClientSide) {
            entity.discard();
        }
        return true; // 返回 true 表示不再调用后续更新
    }
}
