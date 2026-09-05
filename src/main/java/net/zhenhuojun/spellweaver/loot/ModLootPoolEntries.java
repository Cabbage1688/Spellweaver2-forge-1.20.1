package net.zhenhuojun.spellweaver.loot;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.zhenhuojun.spellweaver.Spellweaver;

public class ModLootPoolEntries {
    public static final DeferredRegister<LootPoolEntryType> LOOT_POOL_ENTRY_TYPES =
            DeferredRegister.create(Registries.LOOT_POOL_ENTRY_TYPE, Spellweaver.MODID);

    public static final RegistryObject<LootPoolEntryType> MAGIC_PAGE =
            LOOT_POOL_ENTRY_TYPES.register("magic_page",
                    () -> new LootPoolEntryType(new MagicPageLootEntry.Serializer()));

    public static void register(IEventBus modEventBus) {
        LOOT_POOL_ENTRY_TYPES.register(modEventBus);
    }
}
