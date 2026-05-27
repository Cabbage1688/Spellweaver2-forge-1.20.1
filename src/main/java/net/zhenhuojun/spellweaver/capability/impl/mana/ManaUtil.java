package net.zhenhuojun.spellweaver.capability.impl.mana;

import com.google.common.util.concurrent.AtomicDouble;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.zhenhuojun.spellweaver.block.custom.SpellMachineBlockEntity;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerManaProvider;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.ManaChangeS2CPacket;
import net.zhenhuojun.spellweaver.spell.SpellContext;

import java.util.concurrent.atomic.AtomicBoolean;

//封装一些逻辑
public class ManaUtil {
    //用于检查魔力与最大魔力差
    public static double CheckMaxManaDifference(ServerPlayer player){
        AtomicDouble manaDifference = new AtomicDouble();
        manaDifference.set(0d);
        if(player!=null){
            player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(playerMana -> {
                manaDifference.set(playerMana.getMaxMana()-playerMana.getMana());
            });
        }
        return manaDifference.get();
    }
    //检查现有魔力值
    public static double CheckMana(ServerPlayer player){
        AtomicDouble mana = new AtomicDouble();
        mana.set(0d);
        if(player!=null){
            player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(playerMana -> {
                mana.set(playerMana.getMana());
            });
        }
        return mana.get();
    }

    public static void addManaAndSendPacket(double add, ServerPlayer player){
        if(player!=null){
            player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(playerMana -> {
                playerMana.addMana(add);
                ModMessage.sendToPlayer(new ManaChangeS2CPacket(playerMana.getMana(), playerMana.getMaxMana()
                        ,playerMana.getMana_level()), player);
            });
        }
    }
    //2026.5.26
    public static void subManaBecauseOfManaBottleButNotAddExpAndSendPacket(double sub, ServerPlayer player){
        if(player!=null){
            //AtomicBoolean result = new AtomicBoolean(false);
            player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(playerMana -> {
                if(playerMana.getMana()>=sub){
                    playerMana.subMana(sub);
                    ModMessage.sendToPlayer(new ManaChangeS2CPacket(playerMana.getMana(), playerMana.getMaxMana()
                            ,playerMana.getMana_level()), player);
                   // result.set(true);
                }
            });
           // result.get();
        }
    }
    //布尔值显示魔力是否消耗成功
    public static boolean subManaAndAddExpAndSendPacket(double sub,ServerPlayer player){
        if(player!=null){
            AtomicBoolean result = new AtomicBoolean(false);
            player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(playerMana -> {
                if(playerMana.getMana()>=sub){
                    playerMana.subManaAndAddExp(sub);
                    ModMessage.sendToPlayer(new ManaChangeS2CPacket(playerMana.getMana(), playerMana.getMaxMana()
                            ,playerMana.getMana_level()), player);
                    result.set(true);
                }
            });
            return result.get();
        }
        return false;
    }

    public static boolean subManaAndAddExpAndSendPacket(double sub, SpellContext context){
        ServerPlayer player= (ServerPlayer) context.player;
        ManaSource manaSource=context.manaSource;
        if(player!=null){
            switch(manaSource){
                case PLAYER ->{
                    AtomicBoolean result = new AtomicBoolean(false);
                    player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(playerMana -> {
                        if(playerMana.getMana()>=sub){
                            playerMana.subManaAndAddExp(sub);
                            ModMessage.sendToPlayer(new ManaChangeS2CPacket(playerMana.getMana(), playerMana.getMaxMana()
                                    ,playerMana.getMana_level()), player);
                            result.set(true);
                        }
                    });
                    return result.get();
                }
                case SCROLL -> {
                    boolean result=false;
                    ItemStack scroll =player.getMainHandItem();
                    if (!player.level().isClientSide()&&(scroll.is(ModItems.USED_SCROLL.get())||scroll.is(ModItems.USED_LAZULI_SCROLL.get()))) {
                        CompoundTag aTag = scroll.getTag();
                        if (aTag != null && aTag.contains("scrollSpell")) {
                            CompoundTag spellData = aTag.getCompound("scrollSpell");
                            if(spellData.contains("mana")){
                                double mana= spellData.getDouble("mana");
                                if(mana>=sub){
                                    mana=mana-sub;
                                    spellData.putDouble("mana",mana);
                                    // 每点魔力消耗1点耐久
                                    int damageAmount = (int) Math.ceil(sub);
                                    //hurtAndBreak会自动处理耐久耗尽时的物品破碎
                                    scroll.hurtAndBreak(damageAmount, player, (p) -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
                                    player.getInventory().setChanged();
                                    result=true;
                                }
                            }
                        }
                    }
                    return result;
                }
                case STICK -> {
                    ItemStack spellStick =player.getMainHandItem();
                    if(!player.level().isClientSide&&spellStick.is(ModItems.SPELL_STICK.get())){
                        CompoundTag tag=spellStick.getTag();
                        if(tag!=null&&tag.contains("stickSpell")){
                            AtomicBoolean result = new AtomicBoolean(false);
                            player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(playerMana -> {
                                //法杖施法魔力消耗降低
                                if(playerMana.getMana()>=0.8*sub){
                                    playerMana.subManaAndAddExp(0.8*sub);
                                    ModMessage.sendToPlayer(new ManaChangeS2CPacket(playerMana.getMana(), playerMana.getMaxMana()
                                            ,playerMana.getMana_level()), player);
                                    result.set(true);
                                }
                            });
                            return result.get();
                        }
                    }
                }
                case MACHINE -> {
                    BlockPos pos = context.getMachinePos();
                    if (pos == null) return false;
                    BlockEntity be = context.level.getBlockEntity(pos);
                    if (be instanceof SpellMachineBlockEntity machine) {
                        if (machine.getMana() >= sub) {
                            machine.setMana(machine.getMana() - sub);
                            // 同步到客户端
                            machine.setChanged();
                            context.level.sendBlockUpdated(pos, machine.getBlockState(), machine.getBlockState(), Block.UPDATE_ALL);
                            return true;
                        }
                    }
                    return false;
                }
            }
        }
        return false;
    }

    public static void addManaExpOrAwakeManaByMoonPearlAndSendPacket(ServerPlayer player){
        if(player!=null){
            player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(playerMana -> {
                if(playerMana.getMana_level()==0){
                    playerMana.setMana_level(1);
                    playerMana.addMana(100);
                    player.displayClientMessage(
                            Component.literal("你感觉身体的深处涌出了一股暖流。").withStyle(ChatFormatting.LIGHT_PURPLE),
                            true
                    );
                }else{
                    playerMana.addExp(100);
                }
                ModMessage.sendToPlayer(new ManaChangeS2CPacket(playerMana.getMana(), playerMana.getMaxMana()
                        ,playerMana.getMana_level()), player);
            });

        }
    }
}
