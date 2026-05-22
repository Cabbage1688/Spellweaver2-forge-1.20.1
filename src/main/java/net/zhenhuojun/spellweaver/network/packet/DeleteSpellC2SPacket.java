package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerSpellStorageProvider;

import java.util.UUID;
import java.util.function.Supplier;

public class DeleteSpellC2SPacket {
    private final UUID uuid;

    public DeleteSpellC2SPacket(UUID uuid){
        this.uuid=uuid;
    }

    public DeleteSpellC2SPacket(FriendlyByteBuf buf){
        this.uuid=buf.readUUID();
    }

    public void toByte(FriendlyByteBuf buf){
        buf.writeUUID(uuid);
    }
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {

            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(PlayerSpellStorageProvider.PLAYER_SPELL_STORAGE).ifPresent(playerSpellStorage -> {
                    if(playerSpellStorage.getSpell(uuid).isPresent())
                        playerSpellStorage.removeSpell(uuid);
                });
            }

        });
        return true;
    }
}
