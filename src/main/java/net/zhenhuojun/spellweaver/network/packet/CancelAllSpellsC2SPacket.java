package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.spell.SpellTreeExecuteManager;

import java.util.function.Supplier;

public class CancelAllSpellsC2SPacket {
    public CancelAllSpellsC2SPacket() {}
    public CancelAllSpellsC2SPacket(FriendlyByteBuf buf) {}
    public void toByte(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                SpellTreeExecuteManager.getInstance().cancelAllSpellsForPlayer(player);
            }
        });
        return true;
    }
}