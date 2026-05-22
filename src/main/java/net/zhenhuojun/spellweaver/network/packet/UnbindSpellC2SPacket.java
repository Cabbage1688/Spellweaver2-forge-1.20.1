package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerSpellStorageProvider;
import net.zhenhuojun.spellweaver.network.ModMessage;

import java.util.UUID;
import java.util.function.Supplier;

public class UnbindSpellC2SPacket {
    private final int slot;

    public UnbindSpellC2SPacket(int slot) {
        this.slot = slot;
    }

    public UnbindSpellC2SPacket(FriendlyByteBuf buf) {
        this.slot = buf.readInt();
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeInt(slot);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            player.getCapability(PlayerSpellStorageProvider.PLAYER_SPELL_STORAGE).ifPresent(storage -> {
                storage.unbindSpell(slot);
                ModMessage.sendToPlayer(new SpellStorageSyncS2CPacket(storage.serialize()), player);
            });
        });
        return true;
    }
}