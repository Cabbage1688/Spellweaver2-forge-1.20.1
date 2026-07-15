package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.item.custom.ErasingKnifeItem;
import net.zhenhuojun.spellweaver.item.util.SpellBlockStorage;
import net.zhenhuojun.spellweaver.network.ModMessage;

import java.util.function.Supplier;

public class MagicBrushClearC2SPacket {
    private final boolean clearBlock;
    private final BlockPos blockPos;

    public MagicBrushClearC2SPacket(boolean clearBlock, BlockPos blockPos) {
        this.clearBlock = clearBlock;
        this.blockPos = blockPos;
    }

    public MagicBrushClearC2SPacket(FriendlyByteBuf buf) {
        this.clearBlock = buf.readBoolean();
        this.blockPos = clearBlock ? buf.readBlockPos() : null;
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeBoolean(clearBlock);
        if (clearBlock) {
            buf.writeBlockPos(blockPos);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Spellweaver.getLOGGER().debug("[Spellweaver:MagicBrushClearC2SPacket/handle]已接收到法术清除包");
            ServerPlayer player = context.getSender();
            if (player == null) return;

            ItemStack mainHand = player.getMainHandItem();
            if (!(mainHand.getItem() instanceof ErasingKnifeItem)) {
                return;
            }
            Spellweaver.getLOGGER().debug("[Spellweaver:MagicBrushClearC2SPacket/handle]刷子不为空");
            if (clearBlock) {
                if (blockPos != null) {
                    clearSpellFromBlock(player.serverLevel(), blockPos);
                    player.sendSystemMessage(Component.literal("§a已清除方块 " + blockPos.getX()+" "+blockPos.getY()+" "+blockPos.getZ()+ " 上的法术"));
                }
            } else {
                ItemStack offhand = player.getOffhandItem();
                if (offhand.isEmpty()) {
                    player.sendSystemMessage(Component.literal("§c你的副手没有物品！"));
                    return;
                }
                if (ModItems.isStaffOrScroll(offhand)) {
                    player.sendSystemMessage(Component.literal("§c无法对法杖或卷轴使用刮刀！"));
                    return;
                }
                if (!offhand.hasTag() || !offhand.getTag().contains("SpellData")) {
                    player.sendSystemMessage(Component.literal("§c副手物品没有法术！"));
                    return;
                }
                clearSpellFromItem(offhand);
                player.sendSystemMessage(Component.literal("§a已清除副手 " + offhand.getDisplayName().getString() + " 上的法术"));
            }
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

    private void clearSpellFromBlock(ServerLevel level, BlockPos pos) {
        SpellBlockStorage storage = SpellBlockStorage.get(level);
        storage.remove(pos);
        ModMessage.sendToClientsInLevel(new SpellBlockSyncS2CPacket(storage.getSpellBlockPositions()), level);
    }
}