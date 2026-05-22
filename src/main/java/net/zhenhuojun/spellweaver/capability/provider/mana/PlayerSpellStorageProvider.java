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
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.PlayerSpellStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerSpellStorageProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<PlayerSpellStorage> PLAYER_SPELL_STORAGE= CapabilityManager.get(new CapabilityToken<>() {});

    private  final PlayerSpellStorage instance;
    private  final LazyOptional<PlayerSpellStorage> optional;

    public PlayerSpellStorageProvider(Player player){
        this.instance=new PlayerSpellStorage(player);
        this.optional=LazyOptional.of(() -> this.instance);
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return PLAYER_SPELL_STORAGE.orEmpty(cap,optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return  instance.serialize();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.deserialize(nbt);
    }
}
