package net.zhenhuojun.spellweaver.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.capability.impl.mana.ManaUtil;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.ManaBottleC2SPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ManaBottleItem extends Item {
    public static final double MAX_MANA = 2048.0; // 物品最大魔力值

    public ManaBottleItem(Properties pProperties) {
        super(pProperties);
    }


    @Override
    public boolean isBarVisible(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("mana") && tag.getDouble("mana") >= 0&&tag.getDouble("mana")<MAX_MANA;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("mana")) return 0;
        double mana = tag.getDouble("mana");
        // 比例 * 13（原版条最大宽度）
        return Math.round((float) (mana / MAX_MANA) * 13.0F);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return //0x3B78FF;
                0x477CFF;
    }



    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("mana")) {
            double mana=tag.getDouble("mana");
            String formattedMana = String.format("%.1f", mana);
            tooltip.add(
                    Component.literal("存储魔力")
                            .append(": ")
                            .append(formattedMana)
                            .withStyle(ChatFormatting.LIGHT_PURPLE)
            );
        }else{
            tooltip.add(
                    Component.literal("未存储魔力").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 开始使用物品
        player.startUsingItem(hand);
        // 返回 consume 表示物品已被“消耗”（其实并没有真正消耗，只是防止再次右键触发）
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        super.releaseUsing(stack, level, entity, timeCharged);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide()) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if(!player.isCrouching()){
            Spellweaver.getLOGGER().debug("[Spellweaver:ManaBottlePacket/handle方法]玩家站立");
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains("mana")) {
                double manaInItem = tag.getDouble("mana");
                if (manaInItem <= 0) return;
                // 计算实际可提取量
                double maxExtract = Math.min(8, manaInItem);
                double availableSpace = ManaUtil.CheckMaxManaDifference(player);
                if (availableSpace <= 0) return;
                double extractAmount = Math.min(maxExtract, availableSpace);
                // 更新物品中的魔力值
                double newManaInItem = manaInItem - extractAmount;
                tag.putDouble("mana", newManaInItem);
                // 更新玩家魔力
                ManaUtil.addManaAndSendPacket(extractAmount, player);
            }
        }else {
            Spellweaver.getLOGGER().debug("[Spellweaver:ManaBottlePacket/handle方法]玩家蹲下");
            CompoundTag tag = stack.getOrCreateTag();
            Spellweaver.getLOGGER().debug("[Spellweaver:ManaBottlePacket/handle方法]tag：{}",tag);
            if (tag.contains("mana")) {
                Spellweaver.getLOGGER().debug("[Spellweaver:ManaBottlePacket/handle方法]包含mana标签");
                double manaInItem = tag.getDouble("mana");
                Spellweaver.getLOGGER().debug("[Spellweaver:ManaBottlePacket/handle方法]物品魔力：{}",manaInItem);
                if (manaInItem < 0) return;
                double maxExtract = Math.min(8, ManaUtil.CheckMana(player));
                double availableSpace=2048-manaInItem;
                if (availableSpace <= 0) return;
                double extractAmount = Math.min(maxExtract, availableSpace);
                double newManaInItem = manaInItem + extractAmount;
                tag.putDouble("mana", newManaInItem);
                Spellweaver.getLOGGER().debug("[Spellweaver:ManaBottlePacket/handle方法]物品魔力更新：{}",newManaInItem);
                //ManaUtil.subManaAndAddExpAndSendPacket(extractAmount,player);
                //2026.5.26现在给魔力瓶充能不再获得魔力经验值
                ManaUtil.subManaBecauseOfManaBottleButNotAddExpAndSendPacket(extractAmount,player);
                Spellweaver.getLOGGER().debug("[Spellweaver:ManaBottlePacket/handle方法]玩家注入魔力：{}",extractAmount);
            }else {
                double newManaInItem = Math.min(8, ManaUtil.CheckMana(player));
                tag.putDouble("mana",newManaInItem);
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("mana")&&tag.getDouble("mana")==MAX_MANA) {
           return true;
        }
        return false;
    }

}
