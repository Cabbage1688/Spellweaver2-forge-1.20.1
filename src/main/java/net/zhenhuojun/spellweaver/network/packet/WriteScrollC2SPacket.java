package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.capability.impl.scroll.ScrollSpellHelper;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerSpellStorageProvider;
import net.zhenhuojun.spellweaver.capability.provider.mana.ScrollSpellProvider;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;

import java.util.function.Supplier;

public class WriteScrollC2SPacket {
    private String spellName;
    private CompoundTag spellTag;

    public WriteScrollC2SPacket(String spellName, CompoundTag spellTag){
        this.spellName=spellName;
        this.spellTag=spellTag;
    }

    public WriteScrollC2SPacket(FriendlyByteBuf buf){
        this.spellName=buf.readUtf();
        this.spellTag=buf.readNbt();
    }
    public void toByte(FriendlyByteBuf buf){
        buf.writeUtf(spellName);
        buf.writeNbt(spellTag);
    }
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ItemStack heldItem = player.getMainHandItem();
                //TODO这个逻辑有时间可以优化一下
                if (!heldItem.isEmpty() && heldItem.is(ModItems.SCROLL.get())) {
                    heldItem.shrink(1);
                    ItemStack usedScroll=new ItemStack(ModItems.USED_SCROLL.get(),1);
                    CompoundTag tag = new CompoundTag();
                    tag.putString("name", spellName);
                    tag.put("sequence",spellTag);
                    tag.putDouble("mana",256);
                    CompoundTag scrollTag = usedScroll.getOrCreateTag();
                    scrollTag.put("scrollSpell", tag);
                    // 给予玩家卷轴
                    if (!player.addItem(usedScroll)) {
                        player.drop(usedScroll, false);
                    }
                    ///青金石卷轴的处理
                } else if (!heldItem.isEmpty() && heldItem.is(ModItems.LAZULI_SCROLL.get())) {
                    heldItem.shrink(1);
                    ItemStack usedScroll=new ItemStack(ModItems.USED_LAZULI_SCROLL.get(),1);
                    CompoundTag tag = new CompoundTag();
                    tag.putString("name", spellName);
                    tag.put("sequence",spellTag);
                    tag.putDouble("mana",512);
                    CompoundTag scrollTag = usedScroll.getOrCreateTag();
                    scrollTag.put("scrollSpell", tag);
                    // 给予玩家卷轴
                    if (!player.addItem(usedScroll)) {
                        player.drop(usedScroll, false);
                    }
                    /// 钻石卷轴
                }else if(!heldItem.isEmpty() && heldItem.is(ModItems.DIAMOND_SCROLL.get())){
                    heldItem.shrink(1);
                    ItemStack usedScroll=new ItemStack(ModItems.USED_DIAMOND_SCROLL.get(),1);
                    CompoundTag tag = new CompoundTag();
                    tag.putString("name", spellName);
                    tag.put("sequence",spellTag);
                    tag.putDouble("mana",1024);
                    CompoundTag scrollTag = usedScroll.getOrCreateTag();
                    scrollTag.put("scrollSpell", tag);
                    // 给予玩家卷轴
                    if (!player.addItem(usedScroll)) {
                        player.drop(usedScroll, false);
                    }
                }
            }
        });
        return true;
    }
}
