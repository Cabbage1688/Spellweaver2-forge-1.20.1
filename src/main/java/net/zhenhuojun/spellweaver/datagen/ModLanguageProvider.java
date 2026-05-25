package net.zhenhuojun.spellweaver.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import net.zhenhuojun.spellweaver.item.ModItems;

//该类生成语言文件
public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }
    @Override
    protected void addTranslations() {
        this.add(ModItems.TEST_ITEM.get(), "TestItem");
        this.add(ModItems.MOON_PEARL.get(), "ManaPearl");
        this.add(ModItems.MANA_SWORD.get(), "ManaSword");
        this.add(ModItems.MANA_BOW.get(), "ManaBow");
        this.add(ModItems.SCROLL.get(), "Scroll");
        this.add(ModItems.USED_SCROLL.get(), "ScrollWithSpell");
        this.add(ModItems.SPELL_STICK.get(), "SpellStick");
        this.add(ModItems.MANA_BOTTLE.get(), "ManaFocusBottle");
        this.add(ModItems.LAZULI_SCROLL.get(), "LazuliScroll");
        this.add(ModItems.USED_LAZULI_SCROLL.get(), "LazuliScrollWithSpell");
        this.add(ModItems.MANA_PAPER.get(), "ManaPaper");
        this.add(ModItems.SPELL_MACHINE_ITEM.get(), "SpellMachine");

        this.add("gui.spellweaver.spell_storage.title","SpellBox");
        this.add("gui.cast","Cast");
        this.add("gui.rename","Rename");
        this.add("gui.delete","Delete");
        //this.add("gui.spell_bound","BindSpell");
        this.add("gui.rename_spell.title","RenameScreen");
        this.add("gui.name","SpellName");
        this.add("gui.confirm","Confirm");
        this.add("gui.select_slot.title","SpellBindingScreen");
        this.add("gui.select_slot.prompt", "You can bind spells to slots and then use hotkeys to cast them. The mapping between keys and slots can be viewed in Options -> Controls -> Key Bindings.");

        this.add("key.spellweaver.spell1", "HotKey1");
        this.add("key.spellweaver.spell2", "HotKey2");
        this.add("key.spellweaver.spell3", "HotKey3");
        this.add("key.spellweaver.spell4", "HotKey4");
        this.add("key.spellweaver.spell5", "HotKey5");
        this.add("key.spellweaver.spell6", "HotKey6");
        this.add("key.spellweaver.spell7", "HotKey7");
        this.add("key.spellweaver.spell8", "HotKey8");
        this.add("key.spellweaver.spell9", "HotKey9");
        this.add("key.spellweaver.spell10", "HotKey10");
        this.add("key.spellweaver.overload", "SpellOverload");
        this.add("key.spellweaver.overload_up", "OverloadUp");
        this.add("key.spellweaver.overload_down", "OverloadDown");
        this.add("key.spellweaver.test", "SpellWeaving");
        this.add("key.category.spellweaver.test","SpellWeaver");
        this.add("key.category.spellweaver.spell","SpellWeaver :Casting");

        this.add("gui.unbind","Unbind Slot %d");

        this.add("gui.spellweaver.export", "Export");
        this.add("gui.spellweaver.import", "Import");
        this.add("gui.spellweaver.author_sign", "Sign Spell");
        this.add("gui.spellweaver.export.success", "Spell '%s' copied to clipboard, signed by: %s");
        this.add("gui.author", "Author");
        this.add("gui.paste_here", "Paste spell data");
        this.add("gui.export", "Export");
        this.add("gui.import", "Import");
        this.add("gui.spellweaver.edit_note", "Edit Note");
        this.add("gui.spellweaver.no_note", "No Note");
        this.add("gui.note", "Note");

        this.add("gui.spellweaver.all_authors","All The Authors");






    }
}
