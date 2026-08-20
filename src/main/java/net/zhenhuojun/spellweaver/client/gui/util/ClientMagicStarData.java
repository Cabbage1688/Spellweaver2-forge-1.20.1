package net.zhenhuojun.spellweaver.client.gui.util;

import net.zhenhuojun.spellweaver.capability.impl.spell_storage.SpellListEntry;
import net.zhenhuojun.spellweaver.entity.impl.MagicStarEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 客户端缓存当前打开GUI的魔法之星实体数据。
 * 服务器通过 MagicStarSyncS2CPacket 推送数据到这里，GUI从这里读取数据展示。
 */
public class ClientMagicStarData {
    /** 当前GUI关联的实体ID，-1表示无 */
    private static int currentEntityId = -1;
    /** 当前实体的行动模式（0=FOLLOW,1=STOP,2=SLEEP,3=PATROL） */
    private static int actModeOrdinal = 0;
    /** 四个法术列表：0=攻击,1=自保,2=保护主人,3=日常 */
    private static List<SpellListEntry> attackSpells = new ArrayList<>();
    private static List<SpellListEntry> shieldSpells = new ArrayList<>();
    private static List<SpellListEntry> protectMasterSpells = new ArrayList<>();
    private static List<SpellListEntry> routineSpells = new ArrayList<>();
    /** 被禁用的法术UUID集合（按列表独立） */
    private static Map<Integer, Set<UUID>> disabledSpells = new HashMap<>();

    public static int getCurrentEntityId() { return currentEntityId; }
    public static void setCurrentEntityId(int id) { currentEntityId = id; }

    public static MagicStarEntity.ActMode getActMode() {
        MagicStarEntity.ActMode[] modes = MagicStarEntity.ActMode.values();
        if (actModeOrdinal < 0 || actModeOrdinal >= modes.length) return MagicStarEntity.ActMode.FOLLOW;
        return modes[actModeOrdinal];
    }
    public static void setActModeOrdinal(int ordinal) { actModeOrdinal = ordinal; }

    public static List<SpellListEntry> getAttackSpells() { return attackSpells; }
    public static List<SpellListEntry> getShieldSpells() { return shieldSpells; }
    public static List<SpellListEntry> getProtectMasterSpells() { return protectMasterSpells; }
    public static List<SpellListEntry> getRoutineSpells() { return routineSpells; }

    public static List<SpellListEntry> getListByType(int listType) {
        return switch (listType) {
            case 0 -> attackSpells;
            case 1 -> shieldSpells;
            case 2 -> protectMasterSpells;
            case 3 -> routineSpells;
            default -> throw new IllegalArgumentException("Unknown list type: " + listType);
        };
    }

    public static Set<UUID> getDisabledSpells(int listType) {
        return disabledSpells.computeIfAbsent(listType, k -> new HashSet<>());
    }
    public static boolean isSpellDisabled(int listType, UUID id) {
        Set<UUID> set = disabledSpells.get(listType);
        return set != null && set.contains(id);
    }

    public static void updateAll(int entityId, int modeOrdinal,
                                 List<SpellListEntry> attack, List<SpellListEntry> shield,
                                 List<SpellListEntry> protectMaster, List<SpellListEntry> routine,
                                 Map<Integer, Set<UUID>> disabled) {
        currentEntityId = entityId;
        actModeOrdinal = modeOrdinal;
        attackSpells = new ArrayList<>(attack);
        shieldSpells = new ArrayList<>(shield);
        protectMasterSpells = new ArrayList<>(protectMaster);
        routineSpells = new ArrayList<>(routine);
        disabledSpells = new HashMap<>();
        for (Map.Entry<Integer, Set<UUID>> entry : disabled.entrySet()) {
            disabledSpells.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
    }

    public static void clear() {
        currentEntityId = -1;
        actModeOrdinal = 0;
        attackSpells.clear();
        shieldSpells.clear();
        protectMasterSpells.clear();
        routineSpells.clear();
        disabledSpells.clear();
    }
}
