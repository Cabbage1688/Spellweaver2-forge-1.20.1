package net.zhenhuojun.spellweaver.capability.provider.mana;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.zhenhuojun.spellweaver.capability.impl.overload.PlayerManaOverload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerManaOverloadProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static final Capability<PlayerManaOverload> PLAYER_MANA_OVERLOAD =
            CapabilityManager.get(new CapabilityToken<>() {});

    private final PlayerManaOverload instance;
    private final LazyOptional<PlayerManaOverload> optional;

    public PlayerManaOverloadProvider(){
        this.instance=new PlayerManaOverload();
        this.optional = LazyOptional.of(() -> this.instance);
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return PLAYER_MANA_OVERLOAD.orEmpty(cap,optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
       instance.deserializeNBT(tag);
    }
}
