package net.zhenhuojun.spellweaver.entity.impl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
/// 一个容器实体，用于寄存特效，以解决同种photon特效在同一位置/同一实体中，前一个没结束不能开新的特效的问题
public class PhotonEffectEntity extends Entity {
    private int lifeTime=20;
    private Entity owner;
    private boolean shouldPace=true;

    public PhotonEffectEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public PhotonEffectEntity(EntityType<?> pEntityType, Level pLevel,int lifeTime,@Nullable Entity owner) {
        super(pEntityType, pLevel);
        this.lifeTime=lifeTime;
        if(owner==null){
            shouldPace=false;
        }
    }
    @Override
    public void tick(){
        if(owner!=null&&!owner.isAlive()){
            this.discard();
        }
        if(lifeTime>0){
            lifeTime--;
        }else {
            this.discard();
        }
        if(shouldPace&&owner!=null&&owner.isAlive()){
            this.setPos(owner.xo,owner.yo,owner.zo);
        }
        super.tick();
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if(compound.contains("life_time")){
            this.lifeTime=compound.getInt("life_time");
        }
        if(compound.contains("should_pace")) {
            this.shouldPace = compound.getBoolean("should_pace");
        }
        if(this.level() instanceof ServerLevel serverLevel && compound.contains("owner")){
            this.owner=serverLevel.getEntity(compound.getUUID("owner"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("life_time",lifeTime);
        compound.putBoolean("should_pace",shouldPace);
        if(owner!=null){
            compound.putUUID("owner",owner.getUUID());
        }
    }
}
