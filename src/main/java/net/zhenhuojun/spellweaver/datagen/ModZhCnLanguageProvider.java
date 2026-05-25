package net.zhenhuojun.spellweaver.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import net.zhenhuojun.spellweaver.item.ModItems;

public class ModZhCnLanguageProvider extends LanguageProvider {
    public ModZhCnLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    @Override
    protected void addTranslations() {

        this.add(ModItems.TEST_ITEM.get(), "测试物品");
        this.add(ModItems.MOON_PEARL.get(), "魔珠");
        this.add(ModItems.MANA_SWORD.get(), "幻化之剑");
        this.add(ModItems.MANA_BOW.get(), "幻化之弓");
        this.add(ModItems.SCROLL.get(), "卷轴");
        this.add(ModItems.USED_SCROLL.get(), "封装卷轴");
        this.add(ModItems.SPELL_STICK.get(), "法杖");
        this.add(ModItems.MANA_BOTTLE.get(), "魔力瓶");
        this.add(ModItems.LAZULI_SCROLL.get(), "青金石卷轴");
        this.add(ModItems.USED_LAZULI_SCROLL.get(), "封装青金石卷轴");
        this.add(ModItems.MANA_PAPER.get(), "魔法纸");
        this.add(ModItems.SPELL_MACHINE_ITEM.get(), "施法机");

        this.add("gui.spellweaver.spell_storage.title","法术库");
        this.add("gui.cast","释放");
        this.add("gui.rename","重命名");
        this.add("gui.delete","删除");
        //this.add("gui.spell_bound","法术绑定");
        this.add("gui.rename_spell.title","重命名");
        this.add("gui.name","法术名称");
        this.add("gui.confirm","确认");
        this.add("gui.select_slot.title","法术绑定界面");
        this.add("gui.select_slot.prompt","将法术绑定到槽位，就可以使用快捷键施法了。按键与槽位的对应可在按键绑定中查看");

        this.add("key.spellweaver.spell1", "法术快捷键1");
        this.add("key.spellweaver.spell2", "法术快捷键2");
        this.add("key.spellweaver.spell3", "法术快捷键3");
        this.add("key.spellweaver.spell4", "法术快捷键4");
        this.add("key.spellweaver.spell5", "法术快捷键5");
        this.add("key.spellweaver.spell6", "法术快捷键6");
        this.add("key.spellweaver.spell7", "法术快捷键7");
        this.add("key.spellweaver.spell8", "法术快捷键8");
        this.add("key.spellweaver.spell9", "法术快捷键9");
        this.add("key.spellweaver.spell10", "法术快捷键10");
        this.add("key.spellweaver.overload", "法术超载");
        this.add("key.spellweaver.overload_up", "增加超载倍数");
        this.add("key.spellweaver.overload_down", "减少超载倍数");
        this.add("key.spellweaver.test", "法术编织");
        this.add("key.category.spellweaver.test", "织法者");
        this.add("key.category.spellweaver.spell", "织法者：施法快捷键");

        this.add("gui.unbind","解绑槽位 %d");

        this.add("gui.spellweaver.export", "导出");
        this.add("gui.spellweaver.import", "导入");
        this.add("gui.spellweaver.author_sign", "为法术署名");
        this.add("gui.spellweaver.export.success", "法术 '%s' 已复制到剪贴板，署名：%s");
        this.add("gui.author", "署名");
        this.add("gui.paste_here", "粘贴法术数据");
        this.add("gui.export", "导出");
        this.add("gui.import", "导入");
        this.add("gui.spellweaver.edit_note", "编辑备注");
        this.add("gui.spellweaver.no_note", "暂无备注");
        this.add("gui.note", "备注");

        this.add("gui.spellweaver.all_authors","所有作者");

    }
}
