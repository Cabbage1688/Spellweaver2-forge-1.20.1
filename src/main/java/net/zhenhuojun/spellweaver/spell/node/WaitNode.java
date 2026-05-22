package net.zhenhuojun.spellweaver.spell.node;

import net.minecraft.nbt.CompoundTag;

public class WaitNode implements Node{
    private int WaitingTime=0;
    private int originalTime =0;

    @Override
    public NodeEnum getEnum() {
        return NodeEnum.WAIT;
    }

    @Override
    public String getType() {
        return "wait";
    }

    public void setWaitingTime(int waitingTime) {
        WaitingTime = waitingTime;
        originalTime =waitingTime;
    }

    public int getWaitingTime() {
        return WaitingTime;
    }
    public void waitingTimeSub(){
        WaitingTime--;
    }

    public NodeResult executeWaitNode(){
        if(getWaitingTime()>0){
            waitingTimeSub();
            return NodeResult.RUNNING;
        }else if(getWaitingTime()==0){
            WaitingTime=originalTime;
            return NodeResult.SUCCESS;
        }
        return NodeResult.FAULT;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type",getType());
        tag.putInt("waitingTime",getWaitingTime());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        setWaitingTime(tag.getInt("waitingTime"));
    }
}
