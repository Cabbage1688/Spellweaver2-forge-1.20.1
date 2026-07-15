package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.item.ModItems;

import java.util.function.Supplier;

public class InscriptionTableClearC2SPacket {
    private final InteractionHand hand;

    public InscriptionTableClearC2SPacket(InteractionHand hand) {
        this.hand = hand;
    }

    public InscriptionTableClearC2SPacket(FriendlyByteBuf buf) {
        this.hand = buf.readEnum(InteractionHand.class);
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeEnum(hand);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            ItemStack held = player.getItemInHand(hand);
            if (held.isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.spellweaver.no_item_in_hand").withStyle(net.minecraft.ChatFormatting.RED));
                return;
            }
            if (ModItems.isStaffOrScroll(held)) {
                player.sendSystemMessage(Component.translatable("message.spellweaver.cannot_edit_staff_scroll").withStyle(net.minecraft.ChatFormatting.RED));
                return;
            }

            if (!held.hasTag() || !held.getTag().contains("SpellData")) {
                player.sendSystemMessage(Component.translatable("message.spellweaver.no_spell_on_item").withStyle(net.minecraft.ChatFormatting.RED));
                return;
            }

            clearSpellFromItem(held);
            player.sendSystemMessage(Component.translatable("message.spellweaver.spell_cleared", held.getDisplayName().getString()).withStyle(net.minecraft.ChatFormatting.GREEN));
        });
        return true;
    }

    private void clearSpellFromItem(ItemStack stack) {
        if (stack.hasTag()) {
            stack.getTag().remove("SpellData");
            stack.getTag().remove("TriggerType");
            if (stack.getTag().isEmpty()) {
                stack.setTag(null);
            }
        }
    }
}