package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.block.custom.SpellMachineBlockEntity;
import net.zhenhuojun.spellweaver.capability.impl.scroll.ScrollSpellHelper;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;

import java.util.function.Supplier;

public class SpellMachineWriteC2SPacket {
    private final BlockPos pos;
    private final CompoundTag spellTag;

    public SpellMachineWriteC2SPacket(BlockPos pos, CompoundTag spellTag) {
        this.pos = pos;
        this.spellTag = spellTag;
    }

    public SpellMachineWriteC2SPacket(FriendlyByteBuf buf){
        this.pos=buf.readBlockPos();
        this.spellTag=buf.readNbt();
    }

    public void toByte(FriendlyByteBuf buf){
        buf.writeBlockPos(pos);
        buf.writeNbt(spellTag);
    }
    //存储法术的时候一并存储玩家，以便使用该玩家的持久化变量
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                Level level = player.level();
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof SpellMachineBlockEntity machine) {
                    SequenceNode root = new SequenceNode();
                    root.deserializeNBT(spellTag);
                    machine.setSpellRoot(root);
                    machine.setPlayer(player);
                }
            }
        });
        return true;
    }


}