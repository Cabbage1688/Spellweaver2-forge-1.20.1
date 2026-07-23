package net.zhenhuojun.spellweaver.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.item.custom.*;

import static net.zhenhuojun.spellweaver.block.ModBlocks.*;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Spellweaver.MODID);

    public static final RegistryObject<Item> TEST_ITEM= ITEMS.register("test_item",
            () -> new TestItem(new Item.Properties()));

    public static final RegistryObject<Item> MANA_PEARL =ITEMS.register("moon_pearl",()
    -> new ManaPearlItem(new Item.Properties()));

    public static final RegistryObject<Item> MANA_SWORD=ITEMS.register("mana_sword",()
            ->new ManaSwordItem(ManaSwordItem.STAFF_TIER,1,-2.4f,new Item.Properties()));

    public static final RegistryObject<Item> MANA_BOW = ITEMS.register("mana_bow",
            () -> new ManaBowItem(new Item.Properties().durability(384).defaultDurability(384))); // 耐久

    public static final RegistryObject<Item> TEST_BOW=ITEMS.register("test_bow",()
            -> new TestBowItem(new Item.Properties().durability(384).defaultDurability(384))); // 耐久)

    public static final RegistryObject<Item> MANA_PAPER=ITEMS.register("mana_paper",()->
            new Item(new Item.Properties()));

    public static final RegistryObject<Item> DIM_MANA_PEARL=ITEMS.register("dim_mana_pearl",()->new DimManaPearlItem(new Item.Properties()));

    public static final RegistryObject<Item> SCROLL=ITEMS.register("scroll",()-> new ScrollBeforeItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item>USED_SCROLL=ITEMS.register("used_scroll",()->new ScrollItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SPELL_STICK=ITEMS.register("spell_stick",
            ()->new SpellStickItem(SpellStickItem.STAFF_TIER,1,-2.4f,new Item.Properties()));

    public static  final RegistryObject<Item> MANA_BOTTLE=ITEMS.register("mana_bottle",()->new ManaBottleItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SPELL_MACHINE_ITEM =
            ITEMS.register("spell_machine", () -> new BlockItem(SPELL_MACHINE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> MANA_PEDESTAL_ITEM=
            ITEMS.register("mana_pedestal",()->new BlockItem(MANA_PEDESTAL_BLOCK.get(),new Item.Properties()));
    public static final RegistryObject<Item>  INSCRIPTION_TABLE_ITEM=
            ITEMS.register("inscription_table",()->new BlockItem(INSCRIPTION_TABLE_BLOCK.get(),new Item.Properties()));
    //青金石卷轴
    public static final RegistryObject<Item> LAZULI_SCROLL=ITEMS.register("lazuli_scroll",()->new LazuliBeforeItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> USED_LAZULI_SCROLL=ITEMS.register("used_lazuli_scroll",()->new LazuliItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DIAMOND_SCROLL=ITEMS.register("diamond_scroll",()->new DiamondBeforeScroll(new Item.Properties().stacksTo(1)));

    public static final  RegistryObject<Item> USED_DIAMOND_SCROLL=ITEMS.register("used_diamond_scroll",()->new DiamondScroll(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> FEATHER_PEN=ITEMS.register("feather_pen",()->new FeatherPen(new Item.Properties()));

    public static final RegistryObject<Item> ERASING_KNIFE =ITEMS.register("erasing_knife",()->new ErasingKnifeItem(new Item.Properties()));
    //魔法师之镜
    public static final RegistryObject<Item> MAGICIAN_MIRROR =ITEMS.register("magician_mirror",()->new Item(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static boolean isStaffOrScroll(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(MANA_SWORD.get()) ||
               stack.is(SPELL_STICK.get()) ||
               stack.is(SCROLL.get()) ||
               stack.is(USED_SCROLL.get()) ||
               stack.is(LAZULI_SCROLL.get()) ||
               stack.is(USED_LAZULI_SCROLL.get()) ||
               stack.is(DIAMOND_SCROLL.get()) ||
               stack.is(USED_DIAMOND_SCROLL.get())||
                stack.is(MANA_BOW.get());

    }

}
