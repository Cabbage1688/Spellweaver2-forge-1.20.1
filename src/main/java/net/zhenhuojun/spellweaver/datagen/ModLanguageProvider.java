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
        this.add(ModItems.MANA_PEARL.get(), "ManaPearl");
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
        this.add(ModItems.DIAMOND_SCROLL.get(), "DiamondScroll");
        this.add(ModItems.USED_DIAMOND_SCROLL.get(), "DiamondScrollWithSpell");
        this.add(ModItems.FEATHER_PEN.get(), "FeatherPen");
        this.add(ModItems.ERASING_KNIFE.get(), "ErasingKnife");
        this.add(ModItems.INSCRIPTION_TABLE_ITEM.get(), "InscriptionTable");
        this.add(ModItems.MANA_PEDESTAL_ITEM.get(), "ManaPedestal");
        this.add(ModItems.MAGICIAN_MIRROR.get(), "MagicianMirror");
        this.add(ModItems.DIM_MANA_PEARL.get(), "DimManaPearl");

        this.add("gui.spellweaver.spell_storage.title","SpellBox");
        this.add("gui.cast","Cast");
        this.add("gui.rename","Rename");
        this.add("gui.delete","Delete");
        //this.add("gui.spell_bound","BindSpell");
        this.add("gui.rename_spell.title","RenameScreen");
        this.add("gui.name","SpellName");
        this.add("gui.confirm","Confirm");
        this.add("gui.select_slot.title","SpellBindingScreen");
        this.add("gui.select_slot.prompt", "Bind spells to slots and use hotkeys to cast. Map keys in Controls settings.");

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

        this.add("book.spellweaver.landing_text","It is said that in a small number of lapis lazuli mines, there exists a substance called magic pearls, and by using it, one can knock open the door to magic...");

        this.add("gui.spellweaver.edit_spell", "Edit Spell");
        this.add("gui.spellweaver.save_changes", "Save Changes");
        this.add("gui.spellweaver.cancel_changes", "Cancel Changes");
        this.add("gui.spellweaver.condition_settings", "Condition Settings");
        this.add("gui.spellweaver.condition_true", "Execute child nodes when condition is true");
        this.add("gui.spellweaver.current_state", "Current: %s");
        this.add("gui.spellweaver.loop_count", "Loop Count");
        this.add("gui.spellweaver.spell_loop_count", "Spell Loop Count");
        this.add("gui.spellweaver.wait_count", "Wait Count");
        this.add("gui.spellweaver.spell_wait_count", "Spell Wait Count");
        this.add("gui.spellweaver.rune_editor", "Rune Editor");
        this.add("gui.spellweaver.copy", "Copy");
        this.add("gui.spellweaver.copy_fragment", "Spell fragment copied!");
        this.add("gui.spellweaver.clear_all", "Clear All");
        this.add("gui.spellweaver.return", "Return");
        this.add("gui.spellweaver.constant", "Constant");
        this.add("gui.spellweaver.add_constant", "Add Constant");
        this.add("gui.spellweaver.return_parent", "Return to Parent");
        this.add("gui.spellweaver.execute", "Execute");
        this.add("gui.spellweaver.save", "Save");
        this.add("gui.spellweaver.spell_library", "Spell Library");
        this.add("gui.spellweaver.paste", "Paste");
        this.add("gui.spellweaver.paste_full", "Full spell pasted!");
        this.add("gui.spellweaver.paste_fragment", "Spell fragment added to current node!");
        this.add("gui.spellweaver.copy_full", "Full spell copied!");
        this.add("gui.spellweaver.persistent_variables", "Persistent Variables");
        this.add("gui.spellweaver.no_persistent_variables", "You haven't defined any persistent variables!");
        this.add("gui.spellweaver.name_spell", "Name Spell");
        this.add("gui.spellweaver.spell_name", "Spell Name");
        this.add("gui.spellweaver.name_your_spell", "Name Your Spell");
        this.add("gui.spellweaver.edit_machine_spell", "Edit Machine Spell");
        this.add("gui.spellweaver.edit_inscription_spell", "Edit Inscription Spell");
        this.add("gui.spellweaver.infuse_spell_staff", "Infuse Spell into Staff");
        this.add("gui.spellweaver.test", "Test");

        this.add("message.spellweaver.overload_disabled", "Spell Overload Disabled");
        this.add("message.spellweaver.overload_enabled", "Spell Overload Enabled, Multiplier: %d");
        this.add("message.spellweaver.overload_multiplier", "Overload Multiplier Adjusted to: %d");
        this.add("message.spellweaver.warm_current", "You feel a warm current surging from deep within your body.");
        this.add("message.spellweaver.player_only_command", "This command can only be executed by a player");

        this.add("message.spellweaver.not_enough_mana", "Not enough mana");
        this.add("message.spellweaver.not_enough_mana_scroll", "Not enough mana in scroll");
        this.add("message.spellweaver.not_enough_mana_machine", "Not enough mana in machine");
        this.add("message.spellweaver.not_enough_mana_pedestal", "Not enough mana in pedestal");
        this.add("message.spellweaver.not_enough_mana_bottle", "Not enough mana in bottle");
        this.add("message.spellweaver.bottle_empty", "Bottle has no mana stored");

        this.add("message.spellweaver.no_item_in_hand", "You are not holding anything!");
        this.add("message.spellweaver.cannot_edit_staff_scroll", "Inscription table cannot operate on staffs or scrolls!");
        this.add("message.spellweaver.no_spell_on_item", "No spell on this item!");
        this.add("message.spellweaver.spell_cleared", "Spell cleared from %s");

        this.add("message.spellweaver.binding_tip", "Right-click spell entry to open binding interface");
        this.add("message.spellweaver.spell_inventory", "Spell Inventory %d/%d");
        this.add("message.spellweaver.overwrite_spell", "Overwrite spell?");
        this.add("message.spellweaver.storage_full", "Storage is full!");
        this.add("message.spellweaver.invalid_spell_data", "Invalid spell data!");
        this.add("message.spellweaver.decode_failed", "Failed to decode spell data!");
        this.add("message.spellweaver.cannot_read_spell", "Cannot read spell data!");
        this.add("message.spellweaver.spell_corrupted", "Spell data is corrupted!");
        this.add("message.spellweaver.cannot_import", "Storage is full, cannot import!");
        this.add("message.spellweaver.overwritten", "Spell overwritten: %s");
        this.add("message.spellweaver.imported_new", "Imported as new spell: %s");
        this.add("message.spellweaver.import_success", "Import successful: %s");
        this.add("message.spellweaver.spell_exists", "Spell already exists, overwrite?");

        this.add("message.spellweaver.anonymous", "Anonymous");

        this.add("message.spellweaver.mana_level_up", "Mana Level increased to %d!");

        this.add("gui.spellweaver.add_normal_node", "Add Normal Node");
        this.add("gui.spellweaver.add_loop_node","Add Loop Node");
        this.add("gui.spellweaver.add_condition_node", "Add Condition Node");
        this.add( "gui.spellweaver.add_wait_node", "Add Wait Node");

        this.add("message.spellweaver.rune_param_error", "§cParam Error [%s]: %s");
        this.add("message.spellweaver.unknown_rune", "§6Unknown Rune: %s");




    }
}
