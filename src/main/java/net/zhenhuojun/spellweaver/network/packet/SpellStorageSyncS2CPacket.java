package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.PlayerSpellStorage;
import net.zhenhuojun.spellweaver.client.gui.util.ClientPlayerStorageData;


import java.util.function.Supplier;

public class SpellStorageSyncS2CPacket {
    private final CompoundTag storageData;

    public SpellStorageSyncS2CPacket(CompoundTag storageData){
        this.storageData=storageData;
    }
    public SpellStorageSyncS2CPacket(FriendlyByteBuf buf){
        this.storageData=buf.readNbt();
    }
    public void toByte(FriendlyByteBuf buf){
        buf.writeNbt(storageData);
    }
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Spellweaver.getLOGGER().debug("[Spellweaver:SpellStorageSyncS2CPacket/handle]客户端取得的法术存储tag:{}",storageData);
            PlayerSpellStorage playerSpellStorage=new PlayerSpellStorage(Minecraft.getInstance().player);
            playerSpellStorage.deserialize(storageData);
            ClientPlayerStorageData.setPlayerSpellStorage(playerSpellStorage);
            Spellweaver.getLOGGER().debug("[Spellweaver:SpellStorageSyncS2CPacket/handle]客户端已同步法术存储:{}",ClientPlayerStorageData.getPlayerSpellStorage().serialize());
        });
        return true;
    }
}
