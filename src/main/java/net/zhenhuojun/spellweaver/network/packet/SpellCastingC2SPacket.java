package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.spell.SpellManager;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;
import net.zhenhuojun.spellweaver.spell.util.RunesExecuteMethod;

import java.util.function.Supplier;

public class SpellCastingC2SPacket {
    private  CompoundTag spellTag;

    public SpellCastingC2SPacket(CompoundTag tag){
        this.spellTag=tag;
    }

    public SpellCastingC2SPacket(FriendlyByteBuf buf){
        this.spellTag=buf.readNbt();

    }
    public void toByte(FriendlyByteBuf buf){
        buf.writeNbt(spellTag);
    }
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            ServerLevel level = player.serverLevel();
            SequenceNode sequenceNode=new SequenceNode();
            sequenceNode.deserializeNBT(spellTag);
            RunesExecuteMethod.spellLogic(sequenceNode,level,player);
        });
        return true;
    }
}
