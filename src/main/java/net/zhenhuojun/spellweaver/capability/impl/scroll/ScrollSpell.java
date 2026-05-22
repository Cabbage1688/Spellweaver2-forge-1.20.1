package net.zhenhuojun.spellweaver.capability.impl.scroll;

import net.minecraft.nbt.CompoundTag;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;
import org.jetbrains.annotations.Nullable;

/**
 * 我写出这没用东西一定是对于能力系统路径依赖导致的
 */
public class ScrollSpell {
    private SequenceNode sequenceNode; // 法术的符文序列
    private String spellName; // 法术名称（可选）
    private double mana=0;

    // 创建新卷轴法术

    public ScrollSpell(SequenceNode sequenceNode, String spellName,@Nullable Double mana) {
        this.sequenceNode = sequenceNode;
        this.spellName = spellName;
        if(mana!=null){
            this.mana=mana;
        }
    }

    public ScrollSpell(SequenceNode sequenceNode, String spellName){
        this(sequenceNode,spellName,null);
    }


    public void setSequenceNode(SequenceNode sequenceNode) {
        this.sequenceNode = sequenceNode;
    }

    public SequenceNode getSequenceNode() {
        return sequenceNode;
    }

    public String getSpellName() {
        return spellName;
    }

    // 重命名法术
    public void setSpellName(String name) {
        this.spellName = name;
    }

    // 序列化方法
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", spellName);

        tag.put("sequence",sequenceNode.serializeNBT());

        tag.putDouble("mana",mana);

        return tag;
    }

    // 反序列化方法
    /*public  ScrollSpell deserialize(CompoundTag tag) {
        String name = tag.getString("name");
        SequenceNode sequenceNode=new SequenceNode();
        sequenceNode.deserializeNBT((CompoundTag) tag.get("sequence"));
        int mana=tag.getInt("mana");
        return new ScrollSpell(sequenceNode, name,mana);
    }

     */
    public void deserialize(CompoundTag tag){
        this.spellName=tag.getString("name");
        SequenceNode sequenceNode=new SequenceNode();
        sequenceNode.deserializeNBT((CompoundTag) tag.get("sequence"));
        this.sequenceNode=sequenceNode;
        this.mana=tag.getDouble("mana");


    }
}
