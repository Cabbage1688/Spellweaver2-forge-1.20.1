package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.client.data_util.ClientSpellBlockData;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class SpellBlockSyncS2CPacket {
    private final Set<BlockPos> spellBlocks;

    public SpellBlockSyncS2CPacket(Set<BlockPos> spellBlocks) {
        this.spellBlocks = spellBlocks;
    }

    public SpellBlockSyncS2CPacket(FriendlyByteBuf buf) {
        this.spellBlocks = new HashSet<>();
        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            this.spellBlocks.add(buf.readBlockPos());
        }
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeVarInt(spellBlocks.size());
        for (BlockPos pos : spellBlocks) {
            buf.writeBlockPos(pos);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientSpellBlockData.setBlocks(spellBlocks);
        });
        return true;
    }
}