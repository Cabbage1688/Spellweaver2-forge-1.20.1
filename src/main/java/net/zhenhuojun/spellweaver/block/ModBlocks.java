package net.zhenhuojun.spellweaver.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.block.custom.InscriptionTableBlock;
import net.zhenhuojun.spellweaver.block.custom.MagicSoulFireBlock;
import net.zhenhuojun.spellweaver.block.custom.ManaPedestalBlock;
import net.zhenhuojun.spellweaver.block.custom.SpellMachineBlock;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS= DeferredRegister.create(ForgeRegistries.BLOCKS, Spellweaver.MODID);

    public static final RegistryObject<Block> SPELL_MACHINE_BLOCK =
            BLOCKS.register("spell_machine", () -> new SpellMachineBlock(BlockBehaviour.Properties
                    .of()
                    .strength(0.5f,12)//.requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static final RegistryObject<Block> MANA_PEDESTAL_BLOCK =
            BLOCKS.register("mana_pedestal", () -> new ManaPedestalBlock(BlockBehaviour.Properties
                    .of()
                    .strength(0.5f,12)
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static final RegistryObject<Block> INSCRIPTION_TABLE_BLOCK =
            BLOCKS.register("inscription_table", () -> new InscriptionTableBlock(BlockBehaviour.Properties
                    .of()
                    .strength(0.5f,12)
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static final RegistryObject<Block> MAGIC_SOUL_FIRE_BLOCK =
            BLOCKS.register("magic_soul_fire",()->new MagicSoulFireBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .replaceable()
                    .noCollission()
                    .instabreak()
                    .lightLevel((state) -> 15)
                    .sound(SoundType.WOOL)
                    .pushReaction(PushReaction.DESTROY),20));



    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }

}
