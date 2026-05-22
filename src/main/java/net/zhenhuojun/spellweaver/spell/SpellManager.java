package net.zhenhuojun.spellweaver.spell;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;

public class SpellManager extends SavedData {
    private SequenceNode rootNode;

    public SpellManager() {
        this.rootNode = new SequenceNode();
    }

    public SequenceNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(SequenceNode rootNode) {
        this.rootNode = rootNode;
        setDirty(); // 标记数据需要保存
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        if (rootNode != null) {
            compound.put("spellRoot", rootNode.serializeNBT());
        }
        return compound;
    }

    public static SpellManager load(CompoundTag compound) {
        SpellManager manager = new SpellManager();

        if (compound.contains("spellRoot")) {
            CompoundTag rootTag = compound.getCompound("spellRoot");
            SequenceNode root = new SequenceNode();
            root.deserializeNBT(rootTag);
            manager.setRootNode(root);
        }

        return manager;
    }

    // 获取世界数据实例
    public static SpellManager get(Level level) {
        if (level.isClientSide) {
            throw new RuntimeException("Cannot access SpellManager from client side");
        }

        ServerLevel serverLevel = (ServerLevel) level;
        return serverLevel.getDataStorage().computeIfAbsent(
                SpellManager::load,
                SpellManager::new,
                "spell_data"
        );
    }
}