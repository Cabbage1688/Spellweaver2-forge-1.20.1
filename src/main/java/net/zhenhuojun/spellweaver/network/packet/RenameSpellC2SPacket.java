package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerSpellStorageProvider;

import java.util.UUID;
import java.util.function.Supplier;

public class RenameSpellC2SPacket {
    private final UUID uuid;
    private final String newName;
    private final String originalName;

    public RenameSpellC2SPacket(UUID uuid,String newName,String originalName){
        this.uuid=uuid;
        this.newName=newName;
        this.originalName=originalName;
    }

    public RenameSpellC2SPacket(FriendlyByteBuf buf){
        this.uuid=buf.readUUID();
        this.newName=buf.readUtf();
        this.originalName=buf.readUtf();
    }

    public void toByte(FriendlyByteBuf buf){
        buf.writeUUID(uuid);
        buf.writeUtf(newName);
        buf.writeUtf(originalName);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {

            ServerPlayer player = context.getSender();
            if(player!=null){
                player.getCapability(PlayerSpellStorageProvider.PLAYER_SPELL_STORAGE).ifPresent(playerSpellStorage -> {
                        playerSpellStorage.getSpell(uuid).ifPresent(storedSpell -> storedSpell.rename(newName));
                });
            }



        });
        return true;
    }

}
