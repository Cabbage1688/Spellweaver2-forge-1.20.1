package net.zhenhuojun.spellweaver.item.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.capability.impl.mana.ManaUtil;
import net.zhenhuojun.spellweaver.entity.ModEntities;
import net.zhenhuojun.spellweaver.entity.impl.ManaSlashEntity;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.spell.util.RunesExecuteMethod;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ManaSwordItem extends SwordItem {
    private static final int ATTACK_COOLDOWN_TICKS = 20;
    private static final int MAX_CHARGE_TICKS = 30;//按理来说三秒应该是60tick，应该有个地方有问题
    private static final int USE_DURATION = 72000;

    public ManaSwordItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW; // 复用拉弓动画,看看合不合适吧先
    }



    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {


        // 造成基础伤害
        boolean attacked = super.hurtEnemy(stack, target, attacker);
        if (attacked && attacker instanceof Player player) {
            // 设置玩家的攻击冷却
           // player.getCooldowns().addCooldown(this, ATTACK_COOLDOWN_TICKS);
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

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        if (!(user instanceof Player player)) return;
        int usedTicks = getUseDuration(stack) - remainingUseDuration;
        if (usedTicks > MAX_CHARGE_TICKS) usedTicks = MAX_CHARGE_TICKS;

       /*if(!level.isClientSide){
           double maxMana = ManaUtil.getMaxMana((ServerPlayer) player);
           double costPerTick = (maxMana * 0.01f / 20f);
           if (!ManaUtil.subManaAndAddExpAndSendPacket(costPerTick, (ServerPlayer) player)) {
               // 魔力不足，强制停止使用
               player.stopUsingItem();
               return;
           }
       }
        */
        if(level.isClientSide){
            //TODO蓄力进度显示，到时候做个蓄力条
            float progress = usedTicks / (float) MAX_CHARGE_TICKS;
            player.displayClientMessage(
                    Component.literal(String.format("%.0f%%", progress * 100)),
                    true
            );
            // TODO粒子特效
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeCharged) {
        if (level.isClientSide) return;
        if (!(user instanceof Player player)) return;
        int usedTicks = getUseDuration(stack) - timeCharged;
        if (usedTicks <= 0) return;

        int charge = Math.min(usedTicks, MAX_CHARGE_TICKS);
        float ratio = charge / (float) MAX_CHARGE_TICKS;
        double manaCost=ratio*ManaUtil.getMaxMana((ServerPlayer) player)*0.03;

        boolean isBurst = player.isCrouching();
       // if (isBurst) {
            //performManaBurst(player, manaCOst);
        //} else {

        EntityType<ManaSlashEntity> type;
        int penetrateCount=1;
        if (ratio < 0.33f) {
            type = ModEntities.MANA_SLASH.get();
        } else if (ratio < 0.66f) {
            type = ModEntities.MANA_SLASH_MEDIUM.get();
            penetrateCount=4;
        } else {
            type = ModEntities.MANA_SLASH_LARGE.get();
            penetrateCount=8;
        }
            performManaSlash(player, manaCost,type,penetrateCount);
        //}
    }

    private void performManaSlash(Player player, double manaCost,EntityType<ManaSlashEntity> type,int penetrateCount) {
        if(!ManaUtil.subManaAndAddExpAndSendPacket(23+manaCost, (ServerPlayer) player)) return;

        float damage= (float) (7+manaCost*0.5);
        float d= (float) (1+manaCost*0.0125);
        float speed= (float) (2+manaCost*0.005);
        Spellweaver.getLOGGER().debug("[Spellweaver:ManaSwordItem/performManaSlash]剑气速度为{}",d);
        Spellweaver.getLOGGER().debug("[Spellweaver:ManaSwordItem/performManaSlash]剑气伤害为{}",damage);
        //ManaSlashEntity slash = new ManaSlashEntity(player.level(), player, damage, d);
        ManaSlashEntity slash = new ManaSlashEntity(type, player.level(),player,damage,d,penetrateCount,speed);

        //slash.setSpeed(d);
        player.level().addFreshEntity(slash);

    }
    //TODO先不做了
    private void performManaBurst(Player player, double manaCost) {

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }
}
