package net.zhenhuojun.spellweaver;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.zhenhuojun.spellweaver.block.ModBlockEntities;
import net.zhenhuojun.spellweaver.block.ModBlocks;
import net.zhenhuojun.spellweaver.capability.provider.mana.ScrollSpellProvider;
import net.zhenhuojun.spellweaver.damage_type.ModDamageTypes;
import net.zhenhuojun.spellweaver.entity.ModEntities;
import net.zhenhuojun.spellweaver.item.ModCreativeTable;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.network.ModMessage;
import org.slf4j.Logger;

@Mod(Spellweaver.MODID)
public class Spellweaver {
    public static final String MODID="spellweaver";

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static Logger getLOGGER() {
        return LOGGER;
    }

    public Spellweaver(FMLJavaModLoadingContext context){
        //好像比1.20.4多一步，要从context获取IEventBus而不是直接接收
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        ModMessage.register();
        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);

        ModCreativeTable.register(modEventBus);

    }
    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

    }

}
