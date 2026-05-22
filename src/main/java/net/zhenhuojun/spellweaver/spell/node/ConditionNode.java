package net.zhenhuojun.spellweaver.spell.node;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.spell.SpellContext;

import java.util.ArrayList;
import java.util.List;

public class ConditionNode implements Node {
    private List<Node> childrenNodeList = new ArrayList<>();
    private int index = 0;
    private boolean shouldDo;
    private SpellContext context;
    //默认启动条件为true
    private boolean condition=true;
    //2026.4.10修复嵌套bug
    private boolean evaluated = false;

    @Override
    public NodeEnum getEnum() {
        return NodeEnum.CONDITION;
    }

    @Override
    public String getType() {
        return "condition";
    }

    public SpellContext getContext() {
        return context;
    }

    public void setContext(SpellContext context) {
        this.context = context;
    }

    public boolean getShouldDo() {
        return shouldDo;
    }

    public void setShouldDo(boolean shouldDo) {
        this.shouldDo = shouldDo;
    }

    public void setCondition(boolean condition) {
        this.condition = condition;
    }
    public boolean getCondition(){
        return this.condition;
    }

    public List<Node> getChildrenNodeList() {
        return childrenNodeList;
    }

    public void addChildren(Node node) {
        childrenNodeList.add(node);
    }

    public void killChildren() {
        if (childrenNodeList != null && !childrenNodeList.isEmpty()) {
            childrenNodeList.remove(childrenNodeList.size() - 1);
        }
    }

    public void clearChildren() {
        childrenNodeList.clear();
    }

    public int getIndex() {
        return index;
    }
    public void indexAdd(){
        index++;
    }

    public void resetIndex() {
        index = 0;
    }
    public NodeResult executeConditionNode(){
        /*if(context.isTop(Boolean.class)){
            boolean b=context.pop(Boolean.class);
            setShouldDo(b == condition);
        }else{
            setShouldDo(false);
        }

        if (!shouldDo) {
            return NodeResult.SUCCESS;
        }

         */

        // 仅在第一次 tick 时评估条件
        if (!evaluated) {
            if (context.isTop(Boolean.class)) {
                boolean b = context.pop(Boolean.class);
                setShouldDo(b == condition);
            } else {
                setShouldDo(false);
            }
            evaluated = true;   // 标记已评估
        }

        if (!shouldDo) {
            reset();
            return NodeResult.SUCCESS;
        }
        if(childrenNodeList!=null&&!childrenNodeList.isEmpty()&&index<childrenNodeList.size()){
            Node node=childrenNodeList.get(index);
            switch(node.getEnum()){
                case NORMAL -> {
                    if(node instanceof NormalNode normalNode){
                        //执行法术，成功则增加索引,同步法术上下文,并返回法术树状态为运行中
                        if(normalNode.executeSpell(getContext())==NodeResult.SUCCESS){
                            setContext(normalNode.getContext());
                            indexAdd();
                            return NodeResult.RUNNING;
                        }else{
                            Spellweaver.getLOGGER().debug("[Spellweaver:LoopNode/executeChildrenNodeList方法]loop节点中的normal节点执行结果异常");
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
                            return NodeResult.FAULT;
                        }
                    }
                }
                //意外情况处理
                default -> {
                    Spellweaver.getLOGGER().debug("[Spellweaver:LoopNode/executeChildrenNodeList方法]loop节点列表中存在未知类型节点");
                    return NodeResult.FAULT;
                }
            }
        }else if (childrenNodeList!=null&&!childrenNodeList.isEmpty()&&index>=childrenNodeList.size()){
            //重置索引
            resetIndex();
            reset();
            return NodeResult.SUCCESS;
        }
        return NodeResult.FAULT;
    }

    public void reset() {
        evaluated = false;
        shouldDo = false;
        resetIndex();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", getType());
        tag.putBoolean("shouldDo", shouldDo);
        tag.putBoolean("condition",condition);

        ListTag childrenTag = new ListTag();
        for (Node child : childrenNodeList) {
            childrenTag.add(child.serializeNBT());
        }
        tag.put("childrenNodeList", childrenTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        childrenNodeList.clear();
        ListTag childrenTag = tag.getList("childrenNodeList", Tag.TAG_COMPOUND);
        shouldDo = tag.getBoolean("shouldDo");
        condition=tag.getBoolean("condition");

        for (int i = 0; i < childrenTag.size(); i++) {
            CompoundTag childTag = childrenTag.getCompound(i);
            Node node = NodeRegistry.deserialize(childTag);
            if (node != null) {
                childrenNodeList.add(node);
            }
        }
    }
}