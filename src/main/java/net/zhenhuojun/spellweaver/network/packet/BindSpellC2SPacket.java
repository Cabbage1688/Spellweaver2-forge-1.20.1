package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerSpellStorageProvider;
import net.zhenhuojun.spellweaver.network.ModMessage;

import java.util.UUID;
import java.util.function.Supplier;

public class BindSpellC2SPacket {
    private int slot;
    private UUID uuid;

    public BindSpellC2SPacket(int slot, UUID uuid){
        this.slot=slot;
        this.uuid=uuid;
    }
    public BindSpellC2SPacket(FriendlyByteBuf buf){
        this.slot=buf.readInt();
        this.uuid=buf.readUUID();
    }
    public void toByte(FriendlyByteBuf buf){
        buf.writeInt(slot);
        buf.writeUUID(uuid);
    }
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            ServerLevel level = player.serverLevel();
            player.getCapability(PlayerSpellStorageProvider.PLAYER_SPELL_STORAGE).ifPresent(playerSpellStorage -> {
                playerSpellStorage.bindSpellToSlot(slot,uuid);
                //同步包
                ModMessage.sendToPlayer(new SpellStorageSyncS2CPacket(playerSpellStorage.serialize()),player);
            });

        });
        return true;
    }

}
