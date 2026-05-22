package net.zhenhuojun.spellweaver.capability.provider.mana;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.zhenhuojun.spellweaver.capability.impl.long_term_variables.PlayerLongTermVariablesData;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class PlayerLongTermVariablesProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static final Capability<PlayerLongTermVariablesData> PLAYER_LONG_TERM_VARIABLES =
            CapabilityManager.get(new CapabilityToken<>() {});

    private final PlayerLongTermVariablesData instance;
    private final LazyOptional<PlayerLongTermVariablesData> optional;

    public PlayerLongTermVariablesProvider() {
        this.instance = new PlayerLongTermVariablesData();
        this.optional = LazyOptional.of(() -> this.instance);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return PLAYER_LONG_TERM_VARIABLES.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        instance.saveNBT(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.loadNBT(nbt);
    }
}