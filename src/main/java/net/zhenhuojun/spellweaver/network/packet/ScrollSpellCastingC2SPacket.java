package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.capability.impl.mana.ManaSource;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;
import net.zhenhuojun.spellweaver.spell.util.RunesExecuteMethod;

import java.util.function.Supplier;

public class ScrollSpellCastingC2SPacket {
    private final CompoundTag spellTag;
    private final String manaSource;

    public ScrollSpellCastingC2SPacket(CompoundTag tag, String manaSource){
        this.spellTag=tag;
        this.manaSource=manaSource;
    }

    public ScrollSpellCastingC2SPacket(FriendlyByteBuf buf){
        this.spellTag=buf.readNbt();
        this.manaSource= buf.readUtf();

    }
    public void toByte(FriendlyByteBuf buf){
        buf.writeNbt(spellTag);
        buf.writeUtf(manaSource);
    }
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            ServerLevel level = player.serverLevel();
            SequenceNode sequenceNode=new SequenceNode();
            sequenceNode.deserializeNBT(spellTag);
            switch (manaSource){
                case "scroll"-> {
                    //用新的执行方法
                    Spellweaver.getLOGGER().debug("[Spellweaver:ScrollSpellCastingC2SPacket/handle方法]收到数据包，卷轴将调用法术逻辑");
                    RunesExecuteMethod.spellLogic(sequenceNode, level, player, ManaSource.SCROLL);
                }
                case "stick"->{
                    Spellweaver.getLOGGER().debug("[Spellweaver:ScrollSpellCastingC2SPacket/handle方法]收到数据包，法杖将调用法术逻辑");
                    RunesExecuteMethod.spellLogic(sequenceNode, level, player, ManaSource.STICK);
                }
            }
        });
        return true;
    }
}
