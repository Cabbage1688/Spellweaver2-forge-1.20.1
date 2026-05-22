package net.zhenhuojun.spellweaver.block;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.block.custom.SpellMachineBlockEntity;

import static net.zhenhuojun.spellweaver.block.ModBlocks.SPELL_MACHINE_BLOCK;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES= DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Spellweaver.MODID);

    public static final RegistryObject<BlockEntityType<SpellMachineBlockEntity>> SPELL_MACHINE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("spell_machine",
                    () -> BlockEntityType.Builder.of(SpellMachineBlockEntity::new, SPELL_MACHINE_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus){
        BLOCK_ENTITIES.register(eventBus);
    }
}
