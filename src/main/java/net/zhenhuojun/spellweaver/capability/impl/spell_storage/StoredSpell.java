package net.zhenhuojun.spellweaver.capability.impl.spell_storage;

import net.minecraft.nbt.CompoundTag;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;

import java.util.UUID;

public class StoredSpell {
    private UUID id;//法术的唯一标识符
    private String name; //法术的名称
    private SequenceNode sequenceNode;//法术树根节点

    //用于创建新法术
    public StoredSpell(String name,SequenceNode sequenceNode){
        this.id = UUID.randomUUID();
        this.name = name;
        this.sequenceNode=sequenceNode;
    }
    //这个用来反序列化
    public StoredSpell(UUID id,String name,SequenceNode sequenceNode){
        this.id=id;
        this.name=name;
        this.sequenceNode=sequenceNode;
    }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public SequenceNode getSequenceNode() {
        return sequenceNode;
    }

    public void setSequenceNode(SequenceNode sequenceNode) {
        this.sequenceNode = sequenceNode;
    }

    // 重命名法术
    public void rename(String newName) {
        this.name = newName;
    }

    // 序列化方法
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", id);
        tag.putString("name", name);
        tag.put("sequence_node",sequenceNode.serializeNBT());
        return tag;
    }
    //反序列化
    public static StoredSpell deserialize(CompoundTag tag) {
        UUID id = tag.getUUID("id");
        String name = tag.getString("name");
        CompoundTag sequenceTag= (CompoundTag) tag.get("sequence_node");
        SequenceNode sequenceNode=new SequenceNode();
        sequenceNode.deserializeNBT(sequenceTag);
        return new StoredSpell(id,name,sequenceNode);
    }
}
