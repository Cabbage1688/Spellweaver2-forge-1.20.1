package net.zhenhuojun.spellweaver.capability.impl.spell_storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.zhenhuojun.spellweaver.capability.impl.mana.PlayerMana;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;

import java.util.*;
//大部分代码直接继承一代即可
public class PlayerSpellStorage {
    private static final int MAX_STORED_SPELLS = 27; //最大存储法术数
    private Map<UUID, StoredSpell> spells = new LinkedHashMap<>();//一个LinkedHashMap，按插入顺序存储法术（UUID到StoredSpell的映射）
    private  Player player;

    // 使用固定槽位+UUID绑定
    public final Map<Integer, UUID> slotBindings = new HashMap<>();

    public PlayerSpellStorage(Player player) {
        this.player = player;
    }

    // 新增：绑定法术到快捷键槽位
    public void bindSpellToSlot(int slot, UUID spellId) {
        if (slot < 0 || slot >= MAX_STORED_SPELLS) return;
        //修复重复绑定问题，奇怪了我照搬一代的代码怎么一代没问题现在有问题？？2026.2.10
        //if(getSpellInSlot(slot).isEmpty()){
        //这回好像修好了2026.2.11
        if(slotBindings.containsValue(spellId)){
            Optional<Integer> boundSlot = findBoundSlot(spellId);
            boundSlot.ifPresent(slotBindings::remove);
        }
        slotBindings.remove(slot);
        slotBindings.put(slot, spellId);
        //}
    }

    // 新增：获取槽位绑定法术
    public Optional<StoredSpell> getSpellInSlot(int slot) {
        UUID spellId = slotBindings.get(slot);
        if(spellId!=null) {
            System.out.println("[DEBUG]get spell id");
        }
        return spellId != null ? getSpell(spellId) : Optional.empty();
    }

    //存储一个新法术。如果存储已满则发送消息并返回false，否则创建新法术并存储，发送成功消息，返回true。
    public boolean storeSpell(String name, SequenceNode sequenceNode) {
        if (spells.size() >= MAX_STORED_SPELLS) {
            //player.sendSystemMessage(Component.literal("§c法术存储已满!"));
            return false;
        }

        StoredSpell newSpell = new StoredSpell(name, sequenceNode);
        spells.put(newSpell.getId(), newSpell);
        return true;
    }
    //这个方法专门用于保存法术后的同步，或者说用于单个法术的同步存储,暂时没用上
    public boolean storeSpell(String name,SequenceNode sequenceNode,UUID id){
        if (spells.size() >= MAX_STORED_SPELLS) {
            //player.sendSystemMessage(Component.literal("§c法术存储已满!"));
            return false;
        }
        StoredSpell newSpell=new StoredSpell(id, name, sequenceNode);
        spells.put(newSpell.getId(), newSpell);
        return true;
    }


    //根据UUID获取法术
    public Optional<StoredSpell> getSpell(UUID id) {
        return Optional.ofNullable(spells.get(id));
    }

    //获取所有存储的法术（返回值的集合）
    public Collection<StoredSpell> getAllSpells() {
        return spells.values();
    }

    // 删除法术
    public boolean removeSpell(UUID id) {

        //新增： 清理所有绑定到此法术的槽位
        slotBindings.values().removeIf(id::equals);

        StoredSpell removed = spells.remove(id);
        if (removed != null) {
            player.sendSystemMessage(Component.literal("§c法术'" + removed.getName() + "'已移除"));
            return true;
        }
        return false;
    }

    public Map<UUID, StoredSpell> getSpells() {
        return spells;
    }

    public void copyFrom(PlayerSpellStorage source){
        this.spells=source.getSpells();
    }
    // 序列化存储
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        ListTag spellsTag = new ListTag();
        for (StoredSpell spell : spells.values()) {
            spellsTag.add(spell.serialize());
        }
        tag.put("spells", spellsTag);

        //新增,序列化绑定关系
        CompoundTag bindingsTag = new CompoundTag();
        slotBindings.forEach((slot, spellId) ->
                bindingsTag.putUUID(String.valueOf(slot), spellId));
        tag.put("bindings", bindingsTag);
        return tag;
    }

    // 更新法术名称
    public boolean renameSpell(UUID spellId, String newName) {
        StoredSpell spell = spells.get(spellId);
        if (spell != null) {
            spell.rename(newName);
            return true;
        }
        return false;
    }

    // 反序列化
    public void deserialize(CompoundTag tag) {
        //新增：反序列化绑定关系
        CompoundTag bindingsTag = tag.getCompound("bindings");
        bindingsTag.getAllKeys().forEach(slotStr -> {
            int slot = Integer.parseInt(slotStr);
            UUID spellId = bindingsTag.getUUID(slotStr);
            slotBindings.put(slot, spellId);
        });

        spells.clear();
        ListTag spellsTag = tag.getList("spells", Tag.TAG_COMPOUND);
        for (int i = 0; i < spellsTag.size(); i++) {
            CompoundTag spellTag = spellsTag.getCompound(i);
            StoredSpell spell = StoredSpell.deserialize(spellTag);
            spells.put(spell.getId(), spell);
        }
    }

    public Optional<Integer> findBoundSlot(UUID spellId) {
        return slotBindings.entrySet().stream()
                .filter(entry -> spellId.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst();
    }
    //解绑定
    public void unbindSpell(int slot) {
        if (slot < 0 || slot >= MAX_STORED_SPELLS) return;
        slotBindings.remove(slot);
    }
}
