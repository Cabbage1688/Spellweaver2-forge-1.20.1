package net.zhenhuojun.spellweaver.capability.provider.mana;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.zhenhuojun.spellweaver.capability.impl.mana_shield.ManaShield;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class ManaShieldProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static final Capability<ManaShield> MANA_SHIELD = CapabilityManager.get(new CapabilityToken<>() {});

    private final ManaShield instance;
    private final LazyOptional<ManaShield> optional;

    public ManaShieldProvider() {
        this.instance = new ManaShield();
        this.optional = LazyOptional.of(() -> this.instance);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return MANA_SHIELD.orEmpty(cap, optional);
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