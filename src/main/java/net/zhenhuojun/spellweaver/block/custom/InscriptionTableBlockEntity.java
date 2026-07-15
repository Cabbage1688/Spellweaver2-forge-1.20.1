package net.zhenhuojun.spellweaver.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zhenhuojun.spellweaver.block.ModBlockEntities;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;
import org.jetbrains.annotations.Nullable;

public class InscriptionTableBlockEntity extends BlockEntity {

    private SequenceNode spellRoot;

    public InscriptionTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INSCRIPTION_TABLE_BLOCK_ENTITY.get(), pos, state);
        this.spellRoot = null;
    }

    public SequenceNode getSpellRoot() {
        return spellRoot;
    }

    public void setSpellRoot(SequenceNode root) {
        this.spellRoot = root;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (spellRoot != null) {
            tag.put("spellRoot", spellRoot.serializeNBT());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("spellRoot")) {
            spellRoot = new SequenceNode();
            spellRoot.deserializeNBT(tag.getCompound("spellRoot"));
        } else {
            spellRoot = null;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}