package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.capability.impl.mana.ManaUtil;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerManaProvider;
import net.zhenhuojun.spellweaver.network.ModMessage;

import java.util.function.Supplier;

public class ManaBottleC2SPacket {
    //private final double manaAmount;
    private final String hand;

    public ManaBottleC2SPacket(String hand){
        //this.manaAmount=manaAmount;
        this.hand=hand;
    }

    public ManaBottleC2SPacket(FriendlyByteBuf buf){
        //this.manaAmount=buf.readDouble();
        this.hand=buf.readUtf();
    }

    public void toByte(FriendlyByteBuf buf){
        //buf.writeDouble(manaAmount);
        buf.writeUtf(hand);
    }
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Spellweaver.getLOGGER().debug("[Spellweaver:ManaBottlePacket/handle方法]方法调用");
            ServerPlayer player = context.getSender();
            InteractionHand interactionHand = null;
            if (player != null) {
                //ItemStack heldItem = player.getMainHandItem();
                if(hand.equals("main_hand")){
                    interactionHand=InteractionHand.MAIN_HAND;
                }else if(hand.equals("off_hand")) {
                    interactionHand=InteractionHand.OFF_HAND;
                }
                if(interactionHand!=null){
                    Spellweaver.getLOGGER().debug("[Spellweaver:ManaBottlePacket/handle方法]interactionHand不为空");
                    ItemStack itemStack=player.getItemInHand(interactionHand);
                    boolean playerCrouching=player.isCrouching();
                    if (!playerCrouching) {
                        Spellweaver.getLOGGER().debug("[Spellweaver:ManaBottlePacket/handle方法]玩家站立");
                        CompoundTag tag = itemStack.getOrCreateTag();
                        if (tag.contains("mana")) {
                            //CompoundTag spellData = tag.getCompound("mana");
                            double manaInItem = tag.getDouble("mana");
                            if (manaInItem <= 0) return;

                            // 计算实际可提取量
                            double maxExtract = Math.min(128, manaInItem);
                            double availableSpace = ManaUtil.CheckMaxManaDifference(player);
                            if (availableSpace <= 0) return;

                            double extractAmount = Math.min(maxExtract, availableSpace);

                            // 更新物品中的魔力值
                            double newManaInItem = manaInItem - extractAmount;
                            tag.putDouble("mana", newManaInItem);

                            // 更新玩家魔力
                            ManaUtil.addManaAndSendPacket(extractAmount, player);
                        }
                    } else {
                        Spellweaver.getLOGGER().debug("[Spellweaver:ManaBottlePacket/handle方法]玩家蹲下");
                        CompoundTag tag = itemStack.getOrCreateTag();
                        Spellweaver.getLOGGER().debug("[Spellweaver:ManaBottlePacket/handle方法]tag：{}",tag);
                        if (tag.contains("mana")) {
                            Spellweaver.getLOGGER().debug("[Spellweaver:ManaBottlePacket/handle方法]包含mana标签");
                            double manaInItem = tag.getDouble("mana");
                            Spellweaver.getLOGGER().debug("[Spellweaver:ManaBottlePacket/handle方法]物品魔力：{}",manaInItem);
                            if (manaInItem <= 0) return;
                            double maxExtract = Math.min(128, ManaUtil.CheckMana(player));
                            double availableSpace=2560-manaInItem;
                            if (availableSpace <= 0) return;
                            double extractAmount = Math.min(maxExtract, availableSpace);
                            double newManaInItem = manaInItem + extractAmount;
                            tag.putDouble("mana", newManaInItem);
                            Spellweaver.getLOGGER().debug("[Spellweaver:ManaBottlePacket/handle方法]物品魔力更新：{}",newManaInItem);

                            ManaUtil.subManaAndAddExpAndSendPacket(extractAmount,player);
                            Spellweaver.getLOGGER().debug("[Spellweaver:ManaBottlePacket/handle方法]玩家注入魔力：{}",extractAmount);
                        }else {
                            double newManaInItem = Math.min(128, ManaUtil.CheckMana(player));
                            tag.putDouble("mana",newManaInItem);
                        }

                    }
                }
            }
        });
        return true;
    }
}
