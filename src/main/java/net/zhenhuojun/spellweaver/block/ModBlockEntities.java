package net.zhenhuojun.spellweaver.block;

import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.block.custom.InscriptionTableBlockEntity;
import net.zhenhuojun.spellweaver.block.custom.ManaPedestalBlockEntity;
import net.zhenhuojun.spellweaver.block.custom.SpellMachineBlockEntity;

import java.util.Objects;

import static net.zhenhuojun.spellweaver.block.ModBlocks.*;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES= DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Spellweaver.MODID);
    public static final RegistryObject<BlockEntityType<SpellMachineBlockEntity>> SPELL_MACHINE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("spell_machine",
                    () -> BlockEntityType.Builder.of(SpellMachineBlockEntity::new, SPELL_MACHINE_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<ManaPedestalBlockEntity>> MANA_PEDESTAL_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("mana_pedestal",
                    () -> BlockEntityType.Builder.of(ManaPedestalBlockEntity::new, MANA_PEDESTAL_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<InscriptionTableBlockEntity>> INSCRIPTION_TABLE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("inscription_table",
                    () -> BlockEntityType.Builder.of(InscriptionTableBlockEntity::new, INSCRIPTION_TABLE_BLOCK.get()).build(null));




   /* public static final RegistryObject<BlockEntityType<SpellMachineBlockEntity>> SPELL_MACHINE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("spell_machine",
                    () -> BlockEntityType.Builder.of(SpellMachineBlockEntity::new, SPELL_MACHINE_BLOCK.get())
                            .build(Objects.requireNonNull(Util.fetchChoiceType(References.BLOCK_ENTITY, "spell_machine"))));

    public static final RegistryObject<BlockEntityType<ManaPedestalBlockEntity>> MANA_PEDESTAL_BLOCK_ENTITY=
            BLOCK_ENTITIES.register("mana_pedestal",
                    ()->BlockEntityType.Builder.of(ManaPedestalBlockEntity::new,MANA_PEDESTAL_BLOCK.get())
                            .build(Objects.requireNonNull(Util.fetchChoiceType(References.BLOCK_ENTITY, "mana_pedestal"))));

    */



    public static void register(IEventBus eventBus){
        BLOCK_ENTITIES.register(eventBus);
    }
}
