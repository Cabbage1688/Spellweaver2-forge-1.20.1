package net.zhenhuojun.spellweaver.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.item.custom.*;

import static net.zhenhuojun.spellweaver.block.ModBlocks.SPELL_MACHINE_BLOCK;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Spellweaver.MODID);

    public static final RegistryObject<Item> TEST_ITEM= ITEMS.register("test_item",
            () -> new TestItem(new Item.Properties()));

    public static final RegistryObject<Item> MOON_PEARL=ITEMS.register("moon_pearl",()
    -> new MoonPearlItem(new Item.Properties()));

    public static final RegistryObject<Item> MANA_SWORD=ITEMS.register("mana_sword",()
            ->new ManaSwordItem(ManaSwordItem.STAFF_TIER,1,-2.4f,new Item.Properties()));

    public static final RegistryObject<Item> MANA_BOW = ITEMS.register("mana_bow",
            () -> new ManaBowItem(new Item.Properties().durability(384).defaultDurability(384))); // 耐久

    public static final RegistryObject<Item> TEST_BOW=ITEMS.register("test_bow",()
            -> new TestBowItem(new Item.Properties().durability(384).defaultDurability(384))); // 耐久)

    public static final RegistryObject<Item> MANA_PAPER=ITEMS.register("mana_paper",()->
            new Item(new Item.Properties()));

    public static final RegistryObject<Item> SCROLL=ITEMS.register("scroll",()-> new ScrollBeforeItem(new Item.Properties()));
    public static final RegistryObject<Item>USED_SCROLL=ITEMS.register("used_scroll",()->new ScrollItem(new Item.Properties()));

    public static final RegistryObject<Item> SPELL_STICK=ITEMS.register("spell_stick",
            ()->new SpellStickItem(SpellStickItem.STAFF_TIER,1,-2.4f,new Item.Properties()));

    public static  final RegistryObject<Item> MANA_BOTTLE=ITEMS.register("mana_bottle",()->new ManaBottleItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SPELL_MACHINE_ITEM =
            ITEMS.register("spell_machine", () -> new BlockItem(SPELL_MACHINE_BLOCK.get(), new Item.Properties()));
    //青金石卷轴
    public static final RegistryObject<Item> LAZULI_SCROLL=ITEMS.register("lazuli_scroll",()->new LazuliBeforeItem(new Item.Properties()));

    public static final RegistryObject<Item> USED_LAZULI_SCROLL=ITEMS.register("used_lazuli_scroll",()->new LazuliItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
