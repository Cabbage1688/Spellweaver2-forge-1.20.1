package net.zhenhuojun.spellweaver.spell.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

//一个包装类，用于存储容器来源、物品栏接口和槽位信息。
public class SlotReference {
    private final Level level;
    private final IItemHandler inventory;
    private final int slot;

    // 来源信息
    @Nullable
    private final Entity sourceEntity;
    @Nullable private final BlockPos sourcePos;

    /** 无效的空引用 */
    public static final SlotReference EMPTY = new SlotReference(null, null, -1, null, null);

    public SlotReference(Level level, IItemHandler inventory, int slot,
                         @Nullable Entity sourceEntity, @Nullable BlockPos sourcePos) {
        this.level = level;
        this.inventory = inventory;
        this.slot = slot;
        this.sourceEntity = sourceEntity;
        this.sourcePos = sourcePos;
    }

    public boolean isValid() {
        return inventory != null && slot >= 0 && slot < inventory.getSlots();
    }

    public ItemStack getItem() {
        if (!isValid()) return ItemStack.EMPTY;
        return inventory.getStackInSlot(slot);
    }

    public void setItem(ItemStack stack) {
        if (isValid()) {
            inventory.extractItem(slot, inventory.getSlotLimit(slot), false);
            inventory.insertItem(slot, stack, false);
        }
    }
    //从物品槽取出物品
    public ItemStack extract(int amount, boolean simulate) {
        if (!isValid()) return ItemStack.EMPTY;
        return inventory.extractItem(slot, amount, simulate);
    }
    //向物品槽插入物品
    public ItemStack insert(ItemStack stack, boolean simulate) {
        if (!isValid()) return stack;
        return inventory.insertItem(slot, stack, simulate);
    }

    // Getters，可用于日志或后续逻辑判断来源
    @Nullable public Level getLevel() { return level; }
    @Nullable public Entity getSourceEntity() { return sourceEntity; }
    @Nullable public BlockPos getSourcePos() { return sourcePos; }
    public int getSlot() { return slot; }
    public IItemHandler getInventory() { return inventory; }
}