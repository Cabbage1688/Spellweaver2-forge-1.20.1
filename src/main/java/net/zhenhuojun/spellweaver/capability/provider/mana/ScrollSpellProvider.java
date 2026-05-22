package net.zhenhuojun.spellweaver.capability.provider.mana;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.zhenhuojun.spellweaver.capability.impl.scroll.ScrollSpell;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScrollSpellProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<ScrollSpell> SCROLL_SPELL_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});

    private final ScrollSpell instance;
    private final LazyOptional<ScrollSpell> optional;

    public ScrollSpellProvider(){
        this.instance=new ScrollSpell(new SequenceNode(),"默认法术名称");
        this.optional=LazyOptional.of(() -> this.instance);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return SCROLL_SPELL_CAPABILITY.orEmpty(cap,optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.serialize();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.deserialize(nbt);
    }
}
