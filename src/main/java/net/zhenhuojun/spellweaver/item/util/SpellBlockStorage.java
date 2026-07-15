package net.zhenhuojun.spellweaver.item.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 为什么Capability的数据实现INBTSerializable<CompoundTag>接口而这里继承SavedData呢？
 * 因为数据归属不同，一个归属于Level，一个归属于特定实体。二者存储位置不同，生命周期也不同
 *
 */
public class SpellBlockStorage extends SavedData {
    private final Map<BlockPos, CompoundTag> blockSpells = new HashMap<>();
    
    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, CompoundTag> entry : blockSpells.entrySet()) {
            CompoundTag entryTag = new CompoundTag();

            entryTag.putLong("Pos", entry.getKey().asLong());
            entryTag.put("Spell", entry.getValue());
            list.add(entryTag);
        }
        tag.put("BlockSpells", list);
        return tag;
    }

    public static SpellBlockStorage load(CompoundTag tag) {
        SpellBlockStorage storage = new SpellBlockStorage();
        ListTag list = tag.getList("BlockSpells", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            BlockPos pos = BlockPos.of(entryTag.getLong("Pos"));
            CompoundTag spellTag = entryTag.getCompound("Spell");
            storage.blockSpells.put(pos, spellTag);
        }
        return storage;
    }


    public static SpellBlockStorage get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                SpellBlockStorage::load,  // 加载时使用
                SpellBlockStorage::new,   // 不存在时新建
                "spellweaver_block_spells"
        );
    }


    public void put(BlockPos pos, CompoundTag tag) {
        blockSpells.put(pos, tag);
        setDirty();  // 标记需要保存
    }

    public CompoundTag get(BlockPos pos) {
        return blockSpells.get(pos);
    }

    public void remove(BlockPos pos) {
        blockSpells.remove(pos);
        setDirty();
    }
    //反正客户端渲染只需坐标
    public Set<BlockPos> getSpellBlockPositions() {
        return blockSpells.keySet();
    }
}