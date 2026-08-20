package net.zhenhuojun.spellweaver.capability.impl.spell_storage;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * 法术列表条目：可以是法术或延迟。
 * - 法术条目：包装一个StoredSpell
 * - 延迟条目：记录延迟tick数，施放时遇到则等待指定tick后再施放下一个法术
 * 每个条目有唯一UUID，用于禁用/移动/删除操作。
 */
public class SpellListEntry {
    private final UUID id;
    private final StoredSpell spell;   // null表示延迟条目
    private final int delayTicks;      // >0表示延迟条目

    /** 创建法术条目 */
    public static SpellListEntry ofSpell(StoredSpell spell) {
        return new SpellListEntry(spell.getId(), spell, 0);
    }

    /** 创建延迟条目 */
    public static SpellListEntry ofDelay(int delayTicks) {
        return new SpellListEntry(UUID.randomUUID(), null, delayTicks);
    }

    private SpellListEntry(UUID id, StoredSpell spell, int delayTicks) {
        this.id = id;
        this.spell = spell;
        this.delayTicks = delayTicks;
    }

    public boolean isDelay() { return spell == null; }
    public boolean isSpell() { return spell != null; }

    public UUID getId() { return id; }
    public StoredSpell getSpell() { return spell; }
    public int getDelayTicks() { return delayTicks; }

    /** 序列化 */
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", id);
        if (spell != null) {
            tag.putBoolean("isSpell", true);
            tag.put("spell", spell.serialize());
        } else {
            tag.putBoolean("isSpell", false);
            tag.putInt("delayTicks", delayTicks);
        }
        return tag;
    }

    /** 反序列化（兼容旧数据：无isSpell字段时按法术处理） */
    public static SpellListEntry deserialize(CompoundTag tag) {
        UUID id = tag.hasUUID("id") ? tag.getUUID("id") : UUID.randomUUID();
        if (tag.getBoolean("isSpell") || !tag.contains("isSpell")) {
            // 法术条目（兼容旧数据：无isSpell字段时视为法术）
            StoredSpell spell = StoredSpell.deserialize(tag.getCompound("spell"));
            return new SpellListEntry(tag.contains("id") ? id : spell.getId(), spell, 0);
        } else {
            // 延迟条目
            int delay = tag.getInt("delayTicks");
            return new SpellListEntry(id, null, delay);
        }
    }
}
