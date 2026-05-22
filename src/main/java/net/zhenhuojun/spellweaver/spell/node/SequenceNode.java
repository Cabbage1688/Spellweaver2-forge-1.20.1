package net.zhenhuojun.spellweaver.spell.node;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.spell.SpellContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//法术根节点，法术树的根基
public class SequenceNode implements Node{
    private  List<Node> childrenNodeList;
    private  int index=0;
    private NodeResult state;
    private final UUID uuid;
    private SpellContext context;

    private int overloadMultiplier = 1;

    public SequenceNode(){
        this.uuid=UUID.randomUUID();
        this.state=NodeResult.RUNNING;
        this.childrenNodeList=new ArrayList<>();
    }

    public void setOverloadMultiplier(int overloadMultiplier) {
        this.overloadMultiplier = overloadMultiplier;
    }

    public int getOverloadMultiplier() {
        return overloadMultiplier;
    }

    public UUID getUuid() {
        return uuid;
    }

    public SpellContext getContext() {
        return this.context;
    }

    public void setContext(SpellContext context) {
        this.context = context;
    }

    @Override
    public NodeEnum getEnum() {
        return NodeEnum.SEQUENCE;
    }

    public String getType() {
        return "sequence";
    }

    public List<Node> getChildrenNodeList() {
        return childrenNodeList;
    }
    public void addChildren(Node node){
        childrenNodeList.add(node);
    }
    //移除末尾节点
    public void killChildren(){
        if(childrenNodeList!=null&&!childrenNodeList.isEmpty()){
            childrenNodeList.remove(childrenNodeList.size()-1);
        }
    }
    public void clearChildren(){
        childrenNodeList.clear();
    }

    public int getIndex() {
        return index;
    }
    public void indexAdd(){
        index++;
    }
    public void recoverIndex(){
        index=0;
    }
    public NodeResult getState(){
        return this.state;
    }
    public void setState(NodeResult result){
        state=result;
    }
    @Override
    public CompoundTag serializeNBT(){
        CompoundTag tag = new CompoundTag();
        //tag.putString("type",getEnum().toString());
        tag.putString("type",getType());
        tag.putInt("overloadMultiplier", overloadMultiplier);
        //序列化子节点列表
        ListTag childrenTag = new ListTag();
        for(Node child : childrenNodeList){
            childrenTag.add(child.serializeNBT());
        }
        tag.put("childrenNodeList", childrenTag);
        return tag;
    }
    @Override
    public void deserializeNBT(CompoundTag tag){
        childrenNodeList.clear();
        this.overloadMultiplier = tag.getInt("overloadMultiplier");
        ListTag childrenTag = tag.getList("childrenNodeList", Tag.TAG_COMPOUND);
        for (int i = 0; i < childrenTag.size(); i++) {
            CompoundTag childTag = childrenTag.getCompound(i);
            Node node = NodeRegistry.deserialize(childTag);
            if (node != null) {
                childrenNodeList.add(node);
            }
        }
    }
    /**
     * 它是运行法术树的方法，枚举结果决定法术树的状态
     * SUCCESS代表法术树运行圆满结束，RUNNING代表法术树还在运行中，FAULT则代表法术树运行异常
     */
    public NodeResult executeSpellTree(){
        if(childrenNodeList!=null&&!childrenNodeList.isEmpty()&&index<childrenNodeList.size()){
            Node node=childrenNodeList.get(index);
            switch (node.getEnum()){
                //普通节点处理，后续增加更多节点
                case NORMAL -> {
                    if(node instanceof NormalNode normalNode){
                        //执行法术，成功则增加索引,同步法术上下文,并返回法术树状态为运行中
                        if(normalNode.executeSpell(getContext())==NodeResult.SUCCESS){
                            setContext(normalNode.getContext());
                            indexAdd();
                            return NodeResult.RUNNING;
                        }else{
                            Spellweaver.getLOGGER().debug("[Spellweaver:SequenceNode/executeSpellTree方法]normal节点执行结果异常");
                            return NodeResult.FAULT;
                        }
                    }
                }
                case LOOP -> {
                    if(node instanceof LoopNode loopNode){
                        //loop开始前先保存之前的法术上下文信息
                        loopNode.setContext(getContext());
                        NodeResult result=loopNode.executeChildrenNodeList();
                        if(result== NodeResult.SUCCESS){
                            //同步法术上下文更改,下面也是
                            setContext(loopNode.getContext());
                            indexAdd();
                            return NodeResult.RUNNING;
                        }else if(result== NodeResult.RUNNING){
                            //因为这个方法每tick都会调用，所以这里不同步就会导致这一tick的上下文信息被覆盖
                            setContext(loopNode.getContext());
                            return NodeResult.RUNNING;
                        }else{
                            Spellweaver.getLOGGER().debug("[Spellweaver:SequenceNode/executeSpellTree方法]loop节点执行结果异常，为{}",result);
                            return NodeResult.FAULT;
                        }
                    }

                }
                case WAIT -> {
                    if(node instanceof WaitNode waitNode){
                        NodeResult result=waitNode.executeWaitNode();
                        if(result==NodeResult.SUCCESS){
                            indexAdd();
                            return NodeResult.RUNNING;
                        } else if (result==NodeResult.RUNNING) {
                            return NodeResult.RUNNING;
                        }else {
                            return NodeResult.FAULT;
                        }
                    }
                }
                case CONDITION -> {
                    if(node instanceof ConditionNode conditionNode){
                        conditionNode.setContext(getContext());
                        NodeResult result=conditionNode.executeConditionNode();
                        if(result==NodeResult.SUCCESS){
                            setContext(conditionNode.getContext());
                            indexAdd();
                            return NodeResult.RUNNING;
                        }else if(result== NodeResult.RUNNING){
                            //因为这个方法每tick都会调用，所以这里不同步就会导致这一tick的上下文信息被覆盖
                            setContext(conditionNode.getContext());
                            return NodeResult.RUNNING;
                        }else{
                            Spellweaver.getLOGGER().debug("[Spellweaver:SequenceNode/executeSpellTree方法]condition节点执行结果异常，为{}",result);
                            return NodeResult.FAULT;
                        }
                    }
                }
                //意外情况处理
                default -> {
                    Spellweaver.getLOGGER().debug("[Spellweaver:SequenceNode/executeSpellTree方法]警告！未知节点或意外情况！");
                    return NodeResult.FAULT;
                }
            }//遍历完所有节点后返回运行成功信息
        } else if (childrenNodeList!=null&&!childrenNodeList.isEmpty()&&index>=childrenNodeList.size()) {
            return NodeResult.SUCCESS;
        }
        return NodeResult.FAULT;
    }
    public void tick(){
        setState(executeSpellTree());
    }
}
