package net.zhenhuojun.spellweaver.item.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.capability.impl.mana.ManaUtil;
import net.zhenhuojun.spellweaver.client.gui.SpellNamingScreen;
import net.zhenhuojun.spellweaver.client.gui.item_gui.ScrollEditScreen;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.OpenSpellNamingS2CPacket;
import net.zhenhuojun.spellweaver.network.packet.WriteScrollC2SPacket;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;
import org.jetbrains.annotations.NotNull;

public class LazuliBeforeItem extends PreScroll {
    public static final double MAX_MANA = 512.0;

    public LazuliBeforeItem(Properties pProperties) {
        super(pProperties,MAX_MANA);
    }


    /*public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if(player.isCrouching()){
            player.startUsingItem(hand);
        }else{
            if (level.isClientSide()) {
                Minecraft.getInstance().setScreen(new ScrollEditScreen(stack, level));
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
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
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!(entity instanceof ServerPlayer player)) return;
        if (level.isClientSide())return;
        if(player.isCrouching()) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains("mana")) {
                double manaInItem = tag.getDouble("mana");
                if (manaInItem <= 0) return;
                if(manaInItem<MAX_MANA){
                    double maxExtract = Math.min(8, ManaUtil.CheckMana(player));
                    double availableSpace=MAX_MANA-manaInItem;
                    if (availableSpace <= 0) return;
                    double extractAmount = Math.min(maxExtract, availableSpace);
                    double newManaInItem = manaInItem + extractAmount;
                    tag.putDouble("mana", newManaInItem);
                    ManaUtil.subManaAndAddExpAndSendPacket(extractAmount,player);
                }else if(manaInItem>=MAX_MANA){
                    if(tag.contains("sequence")) {
                        //storeCurrentSpell(tag.getCompound("sequence"));
                        ModMessage.sendToPlayer(new OpenSpellNamingS2CPacket(tag.getCompound("sequence")), player);
                    }
                }
            }else {
                double newManaInItem = Math.min(8, ManaUtil.CheckMana(player));
                tag.putDouble("mana",newManaInItem);
            }
        }
    }

     */

}
