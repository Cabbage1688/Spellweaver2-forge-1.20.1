package net.zhenhuojun.spellweaver.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.zhenhuojun.spellweaver.item.ModItems;

//生成物品的模型文件
public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        this.basicItem(ModItems.TEST_ITEM.get());
        this.basicItem(ModItems.MANA_PEARL.get());
        this.withExistingParent(ModItems.MANA_SWORD.get().toString(), mcLoc("item/handheld"))
                .texture("layer0", modLoc("item/mana_sword"));
        /**
         * 要做这么个拉弓动画，你先得在FMLClientSetupEvent事件里注册pull和pulling，是的，这玩意你继承BowItem后还得自己注册才行
         * 然后json文件里这个pull和pulling的命名空间你得填对，你注册的是什么空间你就填什么
         * 最后你记住
         * 你得按照拉弓力度从小到大的顺序来写这个json文件，不然可能有bug
         */
        ItemModelBuilder pulling0 = getBuilder("mana_bow_pulling_0");
        ItemModelBuilder pulling1 = getBuilder("mana_bow_pulling_1");
        ItemModelBuilder pulling2 = getBuilder("mana_bow_pulling_2");
        getBuilder("mana_bow")
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/mana_bow"))
                .override()
                .predicate(mcLoc("pulling"), 1)
                .model(pulling0)   // 开始拉弓
                .end()
                .override()
                .predicate(mcLoc("pulling"), 1)
                .predicate(mcLoc("pull"), 0.65f)
                .model(pulling1)   // 中等拉力
                .end()
                .override()
                .predicate(mcLoc("pulling"), 1)
                .predicate(mcLoc("pull"), 0.9f)
                .model(pulling2)   // 最大拉力
                .end();

        this.basicItem(ModItems.SCROLL.get());
        this.basicItem(ModItems.USED_SCROLL.get());

        this.withExistingParent(ModItems.SPELL_STICK.get().toString(), mcLoc("item/handheld"))
                .texture("layer0", modLoc("item/spell_stick"));

        this.basicItem(ModItems.MANA_BOTTLE.get());
        this.basicItem(ModItems.LAZULI_SCROLL.get());
        this.basicItem(ModItems.USED_LAZULI_SCROLL.get());
        this.basicItem(ModItems.MANA_PAPER.get());
        withExistingParent(ModItems.SPELL_MACHINE_ITEM.getId().getPath(),
                modLoc("block/spell_machine"));

        withExistingParent(ModItems.MANA_PEDESTAL_ITEM.getId().getPath(),
                modLoc("block/mana_pedestal"));

        withExistingParent(ModItems.INSCRIPTION_TABLE_ITEM.getId().getPath(),
                modLoc("block/inscription_table"));

        this.basicItem(ModItems.DIAMOND_SCROLL.get());
        this.basicItem(ModItems.USED_DIAMOND_SCROLL.get());
        this.basicItem(ModItems.FEATHER_PEN.get());
        this.basicItem(ModItems.ERASING_KNIFE.get());
        this.basicItem(ModItems.MAGICIAN_MIRROR.get());
        this.basicItem(ModItems.DIM_MANA_PEARL.get());
        this.basicItem(ModItems.MAGIC_STAR_ITEM.get());
        this.basicItem(ModItems.MAGIC_PAGE.get());







    }


}
