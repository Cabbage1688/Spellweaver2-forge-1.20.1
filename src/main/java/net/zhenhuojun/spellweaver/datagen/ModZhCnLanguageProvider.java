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
        this.add(ModItems.MANA_PEARL.get(), "魔珠");
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
        this.add(ModItems.DIAMOND_SCROLL.get(), "钻石卷轴");
        this.add(ModItems.USED_DIAMOND_SCROLL.get(), "封装钻石卷轴");
        this.add(ModItems.FEATHER_PEN.get(), "羽毛笔");
        this.add(ModItems.ERASING_KNIFE.get(), "刮刀");
        this.add(ModItems.INSCRIPTION_TABLE_ITEM.get(), "刻写台");
        this.add(ModItems.MANA_PEDESTAL_ITEM.get(), "基座");
        this.add(ModItems.MAGICIAN_MIRROR.get(), "魔法师之镜");
        this.add(ModItems.DIM_MANA_PEARL.get(), "黯淡的魔珠");
        this.add(ModItems.MAGIC_STAR_ITEM.get(), "魔法之星");

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
        this.add("key.spellweaver.cancel_spell", "终止法术");
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
        this.add("book.spellweaver.landing_text","据说，少部分青金石矿中有着一种叫做魔珠的物质，使用它，就能叩开魔法的大门……");

        this.add("gui.spellweaver.edit_spell", "编辑法术");
        this.add("gui.spellweaver.save_changes", "保存更改");
        this.add("gui.spellweaver.cancel_changes", "取消更改");
        this.add("gui.spellweaver.condition_settings", "条件设置");
        this.add("gui.spellweaver.condition_true", "条件为此时执行子节点");
        this.add("gui.spellweaver.current_state", "当前：%s");
        this.add("gui.spellweaver.loop_count", "循环次数");
        this.add("gui.spellweaver.spell_loop_count", "法术循环次数");
        this.add("gui.spellweaver.wait_count", "等待次数");
        this.add("gui.spellweaver.spell_wait_count", "法术等待次数");
        this.add("gui.spellweaver.rune_editor", "符文编辑");
        this.add("gui.spellweaver.copy", "复制");
        this.add("gui.spellweaver.copy_fragment", "法术片段已复制！");
        this.add("gui.spellweaver.clear_all", "清除全部");
        this.add("gui.spellweaver.return", "返回");
        this.add("gui.spellweaver.constant", "常量");
        this.add("gui.spellweaver.add_constant", "添加常量");
        this.add("gui.spellweaver.return_parent", "返回上层");
        this.add("gui.spellweaver.execute", "执行");
        this.add("gui.spellweaver.save", "保存");
        this.add("gui.spellweaver.spell_library", "法术库");
        this.add("gui.spellweaver.paste", "粘贴");
        this.add("gui.spellweaver.paste_full", "完整法术已粘贴！");
        this.add("gui.spellweaver.paste_fragment", "法术片段已加入当前节点！");
        this.add("gui.spellweaver.copy_full", "完整法术已复制！");
        this.add("gui.spellweaver.persistent_variables", "持久变量");
        this.add("gui.spellweaver.no_persistent_variables", "你还没有定义持久化变量！");
        this.add("gui.spellweaver.name_spell", "命名法术");
        this.add("gui.spellweaver.spell_name", "法术名称");
        this.add("gui.spellweaver.name_your_spell", "为法术命名");
        this.add("gui.spellweaver.edit_machine_spell", "编辑机器法术");
        this.add("gui.spellweaver.edit_inscription_spell", "编辑铭刻法术");
        this.add("gui.spellweaver.infuse_spell_staff", "注入法术到法杖");
        this.add("gui.spellweaver.test", "测试");

        this.add("message.spellweaver.overload_disabled", "法术超载关闭");
        this.add("message.spellweaver.overload_enabled", "法术超载启用,超载倍数%d");
        this.add("message.spellweaver.overload_multiplier", "超载倍数调整为%d");
        this.add("message.spellweaver.warm_current", "你感觉身体的深处涌出了一股暖流。");
        this.add("message.spellweaver.player_only_command", "此命令只能由玩家执行");

        this.add("message.spellweaver.not_enough_mana", "魔力不足");
        this.add("message.spellweaver.not_enough_mana_scroll", "卷轴魔力不足");
        this.add("message.spellweaver.not_enough_mana_machine", "机器魔力不足");
        this.add("message.spellweaver.not_enough_mana_pedestal", "基座魔力不足");
        this.add("message.spellweaver.not_enough_mana_bottle", "魔力瓶魔力不足");
        this.add("message.spellweaver.bottle_empty", "魔力瓶未存储魔力");

        this.add("message.spellweaver.no_item_in_hand", "你手中没有物品！");
        this.add("message.spellweaver.cannot_edit_staff_scroll", "刻写台无法对法杖或卷轴操作！");
        this.add("message.spellweaver.no_spell_on_item", "物品上没有法术！");
        this.add("message.spellweaver.spell_cleared", "已清除 %s 上的法术");

        this.add("message.spellweaver.binding_tip", "右键法术条目以打开法术绑定界面");
        this.add("message.spellweaver.spell_inventory", "法术库存%d/%d");
        this.add("message.spellweaver.overwrite_spell", "覆盖法术？");
        this.add("message.spellweaver.storage_full", "存储已满！");
        this.add("message.spellweaver.invalid_spell_data", "无效的法术数据！");
        this.add("message.spellweaver.decode_failed", "法术数据解码失败！");
        this.add("message.spellweaver.cannot_read_spell", "无法读取法术数据！");
        this.add("message.spellweaver.spell_corrupted", "法术数据损坏！");
        this.add("message.spellweaver.cannot_import", "存储已满，无法导入！");
        this.add("message.spellweaver.overwritten", "已覆盖法术：%s");
        this.add("message.spellweaver.imported_new", "已导入为新法术：%s");
        this.add("message.spellweaver.import_success", "导入成功：%s");
        this.add("message.spellweaver.spell_exists", "法术已存在，要覆盖吗？");

        this.add("message.spellweaver.anonymous", "佚名");
        this.add("message.spellweaver.spell_canceled", "已终止所有法术");

        this.add("message.spellweaver.mana_level_up", "魔力等级提升至 %d!");

        this.add("message.spellweaver.rune_param_error", "§c参数错误 [%s]: %s");
        this.add("message.spellweaver.unknown_rune", "§6未知符文: %s");

        this.add("gui.spellweaver.add_normal_node", "添加普通节点");
        this.add("gui.spellweaver.add_loop_node","添加循环节点");
        this.add("gui.spellweaver.add_condition_node", "添加条件节点");
        this.add( "gui.spellweaver.add_wait_node", "添加等待节点");

        this.add("hud.spellweaver.mana","魔力值:%d");

        this.add("message.spellweaver.stack_empty_pop", "栈为空，无法弹出元素");
        this.add("message.spellweaver.type_mismatch", "类型不匹配。期望 {0} 但得到 {1}");

        this.add("gui.spellweaver.mana_level","魔力等级：%d");
        this.add("gui.spellweaver.mana_exp","距离下一等级所需要的经验：%d");

        this.add("gui.edit","编辑法术");
        this.add("gui.spell_bound","绑定法术 [%s] 到 [%d] 槽位");

        // ===== 魔法之星 =====
        this.add("gui.spellweaver.magic_star.title", "魔法之星控制");
        this.add("gui.spellweaver.magic_star.current_mode", "当前模式：%s");
        this.add("gui.spellweaver.magic_star.mode.follow", "跟随");
        this.add("gui.spellweaver.magic_star.mode.stop", "定点");
        this.add("gui.spellweaver.magic_star.mode.sleep", "待机");
        this.add("gui.spellweaver.magic_star.mode.patrol", "巡逻");
        this.add("gui.spellweaver.magic_star.list.attack", "攻击法术");
        this.add("gui.spellweaver.magic_star.list.shield", "自保法术");
        this.add("gui.spellweaver.magic_star.list.protect_master", "护主法术");
        this.add("gui.spellweaver.magic_star.list.routine", "日常法术");
        this.add("gui.spellweaver.magic_star.list.title.0", "攻击法术列表");
        this.add("gui.spellweaver.magic_star.list.title.1", "自保法术列表");
        this.add("gui.spellweaver.magic_star.list.title.2", "护主法术列表");
        this.add("gui.spellweaver.magic_star.list.title.3", "日常法术列表");
        this.add("gui.spellweaver.magic_star.up", "上移");
        this.add("gui.spellweaver.magic_star.down", "下移");
        this.add("gui.spellweaver.magic_star.disable", "禁用");
        this.add("gui.spellweaver.magic_star.note_label", "备注：");
        this.add("gui.spellweaver.add", "添加");
        this.add("gui.spellweaver.magic_star.add.title.0", "从法术库添加 - 攻击");
        this.add("gui.spellweaver.magic_star.add.title.1", "从法术库添加 - 自保");
        this.add("gui.spellweaver.magic_star.add.title.2", "从法术库添加 - 护主");
        this.add("gui.spellweaver.magic_star.add.title.3", "从法术库添加 - 日常");
        this.add("gui.spellweaver.magic_star.add.tip", "点击法术添加，可多选，按返回结束");
        this.add("gui.spellweaver.magic_star.add.added", "已添加法术 '%s'");
        this.add("gui.spellweaver.magic_star.add.already_added", "法术 '%s' 已在列表中");
        this.add("gui.spellweaver.magic_star.add_delay", "延迟");
        this.add("gui.spellweaver.magic_star.delay_display", "延迟%d刻");
        this.add("gui.spellweaver.magic_star.delay_input.title", "输入延迟tick数");
        this.add("gui.spellweaver.magic_star.delay_input.tip", "下一法术延迟释放的tick数");
        this.add("gui.spellweaver.magic_star.delay_input.placeholder", "tick数");
        this.add("gui.spellweaver.magic_star.itemize", "物品化");
        this.add("gui.spellweaver.magic_star.summon_tooltip", "点击以创造魔法之星");
        this.add("gui.spellweaver.magic_star.summon_cost", "消耗魔力：%d");
        this.add("message.spellweaver.magic_star.level_too_low", "魔力等级不足100级，无法召唤魔法之星");

        this.add("message.spellweaver.dim_not_work","黯淡的魔珠已无法为你提供帮助");

    }
}
