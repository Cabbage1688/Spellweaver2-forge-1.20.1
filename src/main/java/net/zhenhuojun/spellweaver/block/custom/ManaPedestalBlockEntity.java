package net.zhenhuojun.spellweaver.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.zhenhuojun.spellweaver.block.ModBlockEntities;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.item.custom.ManaBottleItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ManaPedestalBlockEntity extends BlockEntity {
    public static final int MAX_MANA_BOTTLES = 9;
    private double mana;
    private int currentManaBottle;

    public ManaPedestalBlockEntity( BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.MANA_PEDESTAL_BLOCK_ENTITY.get(), pPos, pBlockState);
        this.mana = 0.0;
        this.currentManaBottle = 0;
    }

    public void setMana(double mana) {
        this.mana = mana;
    }

    public double getMana() {
        return mana;
    }
    public int getCurrentManaBottle() { return currentManaBottle; }

    //放魔力瓶
    public void insertManaBottle(Player player, InteractionHand hand) {
        if (currentManaBottle >= MAX_MANA_BOTTLES) return;

        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof ManaBottleItem)) return;

        CompoundTag tag = stack.getTag();
        double bottleMana = tag != null && tag.contains("mana") ? tag.getDouble("mana") : 0.0;
        mana += bottleMana;
        stack.shrink(1);
        if (stack.isEmpty()) {
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
        currentManaBottle++;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public void extractManaBottles(Player player) {
        if (currentManaBottle <= 0) return;
        double bottleMana;
        if(currentManaBottle>1){
            double bottleMaxMana=ManaBottleItem.MAX_MANA;
            bottleMana=mana-bottleMaxMana*(currentManaBottle-1)>0?mana-bottleMaxMana*(currentManaBottle-1):0;
        }else{
            bottleMana=mana;
        }
        int bottlesToGive =1;
        ItemStack bottleStack = new ItemStack(ModItems.MANA_BOTTLE.get(), bottlesToGive);
        CompoundTag tag = bottleStack.getOrCreateTag();
        if(bottleMana>0){
            tag.putDouble("mana", bottleMana);
        }
        if (!player.getInventory().add(bottleStack)) {
            player.drop(bottleStack, false);
        }
        mana = mana-bottleMana;
        --currentManaBottle;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("mana", mana);
        tag.putInt("currentManaBottle", currentManaBottle);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        mana = tag.getDouble("mana");
        currentManaBottle = tag.getInt("currentManaBottle");
    }


    @Override
    public @NotNull CompoundTag getUpdateTag() {
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
