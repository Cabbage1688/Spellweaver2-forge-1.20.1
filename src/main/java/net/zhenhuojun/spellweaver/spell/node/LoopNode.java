package net.zhenhuojun.spellweaver.spell.node;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.spell.SpellContext;

import java.util.ArrayList;
import java.util.List;

public class LoopNode implements Node{
    private List<Node> childrenNodeList=new ArrayList<>();
    private  int index=0;
    private NodeResult state;
    private int currentTime;

    private int originalCurrentTime;
    private SpellContext context;
    //默认-2，即无限循环
    public LoopNode(){
        currentTime=-2;
    }

    @Override
    public NodeEnum getEnum() {
        return NodeEnum.LOOP;
    }

    @Override
    public String getType() {
        return "loop";
    }

    public SpellContext getContext() {
        return context;
    }
    public void setContext(SpellContext context) {
        this.context = context;
    }

    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
        /// 2026.5.23存储原始次数，用于重置
        this.originalCurrentTime=currentTime;

    }
    public void currentTimeSub(){
        currentTime--;
    }
    public int getCurrentTime(){
        return currentTime;
    }

    public List<Node> getChildrenNodeList() {
        return childrenNodeList;
    }
    public void addChildren(Node node){
        childrenNodeList.add(node);
    }
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
    //运行loop节点并返回其运行状态
    public NodeResult executeChildrenNodeList(){
        // 特殊处理：若当前循环次数为 -1，从上下文中获取实际循环次数
        if (getCurrentTime() == -1) {
            try {
                Double loopCount = context.pop(Double.class);
                if (loopCount != null) {
                    int count = loopCount.intValue();   // 转为整数次数
                    setCurrentTime(count);
                } else {
                    Spellweaver.getLOGGER().debug("[Spellweaver:LoopNode] 无法从上下文获取循环次数，节点执行失败");
                    return NodeResult.FAULT;
                }
            } catch (Exception e) {
                Spellweaver.getLOGGER().debug("[Spellweaver:LoopNode] 获取循环次数时发生异常: {}", e.getMessage());
                return NodeResult.FAULT;
            }
        }
        if(childrenNodeList!=null&&!childrenNodeList.isEmpty()&&index<childrenNodeList.size()&&currentTime!=0){
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
            //运行完一轮后的处理
        } else if (childrenNodeList!=null&&!childrenNodeList.isEmpty()&&index>=childrenNodeList.size()) {
            //次数减1
            if(getCurrentTime()>1){
                currentTimeSub();
                recoverIndex();
                return NodeResult.RUNNING;
                //负数循环次数作为无限循环处理
            } else if (getCurrentTime()<0) {
                recoverIndex();
                return NodeResult.RUNNING;
                //循环次数为0，即loop节点运行完成，返回SUCCESS;
            } else if (getCurrentTime()==1) {
                /// 2026.5.23修复，现在成功后依然回退索引，以便下次使用
                recoverIndex();
                this.currentTime=this.originalCurrentTime;
                return NodeResult.SUCCESS;
            }
        }
        Spellweaver.getLOGGER().debug("[Spellweaver:LoopNode/executeChildrenNodeList方法]loop节点运行结果为FAULT是因为其他原因");
        Spellweaver.getLOGGER().debug("[Spellweaver:LoopNode/executeChildrenNodeList方法]原因可能是currentTime异常为0，我们看看currentTime的值吧:{}",currentTime);
        Spellweaver.getLOGGER().debug("[Spellweaver:LoopNode/executeChildrenNodeList方法]childrenNodeList:{}",getChildrenNodeList());
        Spellweaver.getLOGGER().debug("[Spellweaver:LoopNode/executeChildrenNodeList方法]index:{}",getIndex());
        return NodeResult.FAULT;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type",getType());
        tag.putInt("currentTime", currentTime);
        //tag.putInt("originalCurrentTime",originalCurrentTime);

        //序列化子节点列表
        ListTag childrenTag = new ListTag();
        for(Node child : childrenNodeList){
            childrenTag.add(child.serializeNBT());
        }
        tag.put("childrenNodeList", childrenTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        childrenNodeList.clear();
        ListTag childrenTag = tag.getList("childrenNodeList", Tag.TAG_COMPOUND);
        //currentTime = tag.getInt("currentTime");
        //2026.5.23
        setCurrentTime(tag.getInt("currentTime"));
        for (int i = 0; i < childrenTag.size(); i++) {
            CompoundTag childTag = childrenTag.getCompound(i);
            Node node = NodeRegistry.deserialize(childTag);
            if (node != null) {
                childrenNodeList.add(node);
            }
        }
    }
}
