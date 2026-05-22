package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerSpellStorageProvider;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;

import java.util.function.Supplier;

public class SpellStorageC2SPacket {
    private final String spellName;
    private final CompoundTag spellTag;

    public SpellStorageC2SPacket(String spellName, CompoundTag spellTag){
        this.spellName=spellName;
        this.spellTag=spellTag;
    }
    public SpellStorageC2SPacket(FriendlyByteBuf buf){
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
                player.getCapability(PlayerSpellStorageProvider.PLAYER_SPELL_STORAGE).ifPresent(playerSpellStorage -> {
                    SequenceNode sequenceNode=new SequenceNode();
                    sequenceNode.deserializeNBT(spellTag);
                    playerSpellStorage.storeSpell(spellName,sequenceNode);

                    //优化以后再说吧，现在先把整个法术存储发过去得了
                    ModMessage.sendToPlayer(new SpellStorageSyncS2CPacket(playerSpellStorage.serialize()),player);

                   // playerSpellStorage.get
                   // ModMessage.sendToPlayer(new SpellStorageRespondS2CPacket());
                });
            }
        });
        return true;
    }
}
