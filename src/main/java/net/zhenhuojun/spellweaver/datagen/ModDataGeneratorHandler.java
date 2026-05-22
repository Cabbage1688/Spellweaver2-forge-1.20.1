package net.zhenhuojun.spellweaver.datagen;

import net.minecraft.data.DataProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zhenhuojun.spellweaver.Spellweaver;

@Mod.EventBusSubscriber(modid = Spellweaver.MODID,bus=Mod.EventBusSubscriber.Bus.MOD)
public class ModDataGeneratorHandler {
    //GatherDataEvent是mod总线上的事件,该事件在数据生成过程中触发
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event){
        //ExistingFileHelper是一个辅助类，用于检查文件是否存在，以避免覆盖现有文件
        ExistingFileHelper efh=event.getExistingFileHelper();

        event.getGenerator().addProvider(
                event.includeClient(),//仅客户端生成资源时调用
                (DataProvider.Factory<ModLanguageProvider>) pOutput->new ModLanguageProvider(pOutput,Spellweaver.MODID,"en_us")

        );

        event.getGenerator().addProvider(
                event.includeClient(),
                (DataProvider.Factory<ModItemModelProvider>) pOutput-> new ModItemModelProvider(pOutput, Spellweaver.MODID,efh)
        );
        //中文语言文件
        event.getGenerator().addProvider(
                event.includeClient(),
                (DataProvider.Factory<ModZhCnLanguageProvider>) pOutput->new ModZhCnLanguageProvider(pOutput,Spellweaver.MODID,"zh_cn")
        );

    }
}
