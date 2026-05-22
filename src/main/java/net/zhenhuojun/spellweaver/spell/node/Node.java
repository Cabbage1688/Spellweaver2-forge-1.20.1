package net.zhenhuojun.spellweaver.spell.node;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface Node extends INBTSerializable<CompoundTag> {
    //NodeResult getState();

    NodeEnum getEnum();

    String getType();
}
