package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.client.gui.util.ClientPlayerStorageData;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;

import java.util.UUID;
import java.util.function.Supplier;
//这个包本来准备传递单个法术，避免每次同步都传递整个法术存储
public class SpellStorageRespondS2CPacket {
    private final UUID uuid;
    private final String name;
    private final CompoundTag spellTag;

    public SpellStorageRespondS2CPacket(UUID uuid,String name,CompoundTag spellTag){
        this.uuid=uuid;
        this.name=name;
        this.spellTag=spellTag;
    }

    public SpellStorageRespondS2CPacket(FriendlyByteBuf buf){
        this.uuid=buf.readUUID();
        this.name=buf.readUtf();
        this.spellTag=buf.readNbt();
    }

    public void toByte(FriendlyByteBuf buf){

    }
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            SequenceNode sequenceNode=new SequenceNode();
            sequenceNode.deserializeNBT(spellTag);
            ClientPlayerStorageData.getPlayerSpellStorage().storeSpell(name,sequenceNode,uuid);
        });
        return true;
    }
}
