package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.item.custom.SpellStickItem;

import java.util.function.Supplier;

public class ClearSpellStickC2SPacket {
    public ClearSpellStickC2SPacket() {}
    public ClearSpellStickC2SPacket(FriendlyByteBuf buf) {}
    public void toByte(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ItemStack offhand = player.getOffhandItem();
                if (offhand.getItem() instanceof SpellStickItem) {
                    offhand.removeTagKey("stickSpell");
                }
            }
        });
        return true;
    }
}