package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.block.custom.InscriptionTableBlockEntity;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;

import java.util.function.Supplier;

public class InscriptionTableWriteC2SPacket {
    private final BlockPos pos;
    private final CompoundTag spellTag;
    private final InteractionHand hand;

    public InscriptionTableWriteC2SPacket(BlockPos pos, CompoundTag spellTag) {
        this.pos = pos;
        this.spellTag = spellTag;
        this.hand = null;
    }

    public InscriptionTableWriteC2SPacket(BlockPos pos, CompoundTag spellTag, InteractionHand hand) {
        this.pos = pos;
        this.spellTag = spellTag;
        this.hand = hand;
    }

    public InscriptionTableWriteC2SPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.spellTag = buf.readNbt();
        boolean hasHand = buf.readBoolean();
        this.hand = hasHand ? buf.readEnum(InteractionHand.class) : null;
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeNbt(spellTag);
        buf.writeBoolean(hand != null);
        if (hand != null) {
            buf.writeEnum(hand);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            Level level = player.level();
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof InscriptionTableBlockEntity table)) {
                return;
            }

            if (hand != null) {
                ItemStack held = player.getItemInHand(hand);
                if (held.isEmpty()) {
                    player.sendSystemMessage(Component.literal("§c你手中没有物品！"));
                    return;
                }
                if (ModItems.isStaffOrScroll(held)) {
                    player.sendSystemMessage(Component.literal("§c刻写台无法对法杖或卷轴操作！"));
                    return;
                }

                SequenceNode root = new SequenceNode();
                root.deserializeNBT(spellTag);
                if (root.getChildrenNodeList().isEmpty()) {
                    player.sendSystemMessage(Component.literal("§c法术为空！"));
                    return;
                }

                writeSpellToItem(held, spellTag);
                player.sendSystemMessage(Component.literal("§a已将法术写入 " + held.getDisplayName().getString()));
            } else {
                SequenceNode root = new SequenceNode();
                root.deserializeNBT(spellTag);
                table.setSpellRoot(root);
            }
        });
        return true;
    }

    private void writeSpellToItem(ItemStack stack, CompoundTag spellTag) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put("SpellData", spellTag.copy());
        if (stack.getItem() instanceof ArmorItem) {
            tag.putString("TriggerType", "ON_HURT");
        } else if (stack.getItem() instanceof BlockItem) {
            tag.putString("TriggerType", "ON_PLACE");
        } else {
            tag.putString("TriggerType", "ON_USE");
        }
    }
}