package net.zhenhuojun.spellweaver.capability.provider.mana;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.zhenhuojun.spellweaver.capability.impl.mana.PlayerMana;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class PlayerManaProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<PlayerMana> PLAYER_MANA = CapabilityManager.get(new CapabilityToken<>() {});

    private final PlayerMana instance;
    private final LazyOptional<PlayerMana> optional;

    public PlayerManaProvider(Player player) {
        this.instance = new PlayerMana(player);
        this.optional = LazyOptional.of(() -> this.instance);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return PLAYER_MANA.orEmpty(cap, optional);
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