package net.zhenhuojun.spellweaver;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.zhenhuojun.spellweaver.block.ModBlockEntities;
import net.zhenhuojun.spellweaver.block.ModBlocks;
import net.zhenhuojun.spellweaver.capability.provider.mana.ScrollSpellProvider;
import net.zhenhuojun.spellweaver.damage_type.ModDamageTypes;
import net.zhenhuojun.spellweaver.entity.ModEntities;
import net.zhenhuojun.spellweaver.entity.ai.ModActivities;
import net.zhenhuojun.spellweaver.entity.ai.ModSensors;
import net.zhenhuojun.spellweaver.item.ModCreativeTable;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.loot.ModLootPoolEntries;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.particle.ModParticle;
import org.slf4j.Logger;
/// 致读我的代码的人：如果莫名其妙的遇到模块冲突，可以在cmd里面跑一遍（gradlew clean runClient），跑完可能就好了（如果cmd运行没问题的话）
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
        ModActivities.register(modEventBus);
        ModSensors.register(modEventBus);
        ModParticle.register(modEventBus);

        ModCreativeTable.register(modEventBus);
        ModLootPoolEntries.register(modEventBus);

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

    }
    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

    }

}
