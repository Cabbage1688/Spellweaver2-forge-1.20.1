package net.zhenhuojun.spellweaver.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.block.custom.SpellMachineBlock;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS= DeferredRegister.create(ForgeRegistries.BLOCKS, Spellweaver.MODID);

    public static final RegistryObject<Block> SPELL_MACHINE_BLOCK =
            BLOCKS.register("spell_machine", () -> new SpellMachineBlock(BlockBehaviour.Properties
                    .of()
                    .strength(0.5f,12)//.requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion()));



    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }

}
