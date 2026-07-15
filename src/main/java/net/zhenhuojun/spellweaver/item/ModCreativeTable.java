package net.zhenhuojun.spellweaver.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.zhenhuojun.spellweaver.Spellweaver;

import java.util.function.Supplier;

public class ModCreativeTable {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Spellweaver.MODID);//获得一个创造模式物品栏的注册器
    public static final String MAGIC_MOD_TAB_STRING="Spellweaver2";

    //添加一个tab,title标题,icon图标,displayItems为tab中添加的内容
    public static final Supplier<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("spellweaver_tab",() -> CreativeModeTab.builder()//通过注册器注册创造模式物品栏
            .withTabsBefore(CreativeModeTabs.COMBAT)//物品栏位置，这里是放到了COMBAT之前
            .title(Component.translatable(MAGIC_MOD_TAB_STRING))//这里用translatable的话可以翻译，另一个就直接写死了
            .icon(() -> ModItems.MANA_BOTTLE.get().getDefaultInstance())//物品栏显示图标
            .displayItems((pParameters, pOutput) -> {
                // 功能/材料类
                pOutput.accept(ModItems.DIM_MANA_PEARL.get());
                pOutput.accept(ModItems.MANA_PEARL.get());
                pOutput.accept(ModItems.MANA_PAPER.get());
                pOutput.accept(ModItems.MANA_BOTTLE.get());
                pOutput.accept(ModItems.SCROLL.get());
                //pOutput.accept(ModItems.USED_SCROLL.get());
                pOutput.accept(ModItems.LAZULI_SCROLL.get());
                //pOutput.accept(ModItems.USED_LAZULI_SCROLL.get());
                pOutput.accept(ModItems.DIAMOND_SCROLL.get());
                //pOutput.accept(ModItems.USED_DIAMOND_SCROLL.get());
                // 武器/工具
                pOutput.accept(ModItems.SPELL_STICK.get());
                pOutput.accept(ModItems.FEATHER_PEN.get());
                pOutput.accept(ModItems.ERASING_KNIFE.get());
                pOutput.accept(ModItems.MAGICIAN_MIRROR.get());
                // 方块物品
                pOutput.accept(ModItems.SPELL_MACHINE_ITEM.get());
                pOutput.accept(ModItems.MANA_PEDESTAL_ITEM.get());
                pOutput.accept(ModItems.INSCRIPTION_TABLE_ITEM.get());
            })
            .build());


    public static void register(IEventBus eventBus){CREATIVE_MODE_TABS.register(eventBus);}
}

