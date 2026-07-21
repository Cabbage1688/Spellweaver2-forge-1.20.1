package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.item.custom.FeatherPen;
import net.zhenhuojun.spellweaver.item.util.SpellBlockStorage;
import net.zhenhuojun.spellweaver.network.ModMessage;

import java.util.function.Supplier;

public class FeatherPenSpellC2SPacket {
    private final CompoundTag spellTag;
    private final boolean writeToBlock;
    private final BlockPos blockPos;


    public FeatherPenSpellC2SPacket(CompoundTag spellTag, boolean writeToBlock, BlockPos blockPos) {
        this.spellTag = spellTag;
        this.writeToBlock = writeToBlock;
        this.blockPos = blockPos;
    }


    public FeatherPenSpellC2SPacket(FriendlyByteBuf buf) {
        this.spellTag = buf.readNbt();
        this.writeToBlock = buf.readBoolean();
        this.blockPos = writeToBlock ? buf.readBlockPos() : null;
    }


    public void toByte(FriendlyByteBuf buf) {
        buf.writeNbt(spellTag);
        buf.writeBoolean(writeToBlock);
        if (writeToBlock) {
            buf.writeBlockPos(blockPos);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;


            ItemStack mainHand = player.getMainHandItem();
            if (!(mainHand.getItem() instanceof FeatherPen)) {
                return;
            }

            if (writeToBlock) {
                if (blockPos != null) {
                    writeSpellToBlock(player.serverLevel(), blockPos, spellTag);
                    player.sendSystemMessage(Component.literal("§a已将法术写入 " + blockPos.getX()+" "+blockPos.getY()+" "+blockPos.getZ()+" 处的方块"));
                }
            } else {
                ItemStack offhand = player.getOffhandItem();
                if (offhand.isEmpty()) {
                    player.sendSystemMessage(Component.literal("§c你的副手没有物品！"));
                    return;
                }
                if (ModItems.isStaffOrScroll(offhand)) {
                    player.sendSystemMessage(Component.literal("§c无法对法杖或卷轴使用羽毛笔！"));
                    return;
                }
                writeSpellToItem(offhand, spellTag);
                player.sendSystemMessage(Component.literal("§a已将法术写入副手 " + offhand.getDisplayName().getString()));
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

    public static void writeSpellToBlock(ServerLevel level, BlockPos pos, CompoundTag spellTag) {
        SpellBlockStorage storage = SpellBlockStorage.get(level);
        storage.put(pos, spellTag.copy());
        ModMessage.sendToClientsInLevel(new SpellBlockSyncS2CPacket(storage.getSpellBlockPositions()), level);
    }
}