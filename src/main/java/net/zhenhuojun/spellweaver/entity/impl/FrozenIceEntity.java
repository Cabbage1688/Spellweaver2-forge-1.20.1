package net.zhenhuojun.spellweaver.entity.impl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.entity.ModEntities;

import java.util.Optional;
import java.util.UUID;

public class FrozenIceEntity extends Entity {
    private LivingEntity frozenEntity;
    private int remainingTicks=100;

    private static EntityDataAccessor<Optional<UUID>> FROZEN_ENTITY = SynchedEntityData.defineId(FrozenIceEntity.class,EntityDataSerializers.OPTIONAL_UUID);
    private static EntityDataAccessor<Float> WIDTH = SynchedEntityData.defineId(FrozenIceEntity.class,EntityDataSerializers.FLOAT);
    private static EntityDataAccessor<Float> HEIGHT = SynchedEntityData.defineId(FrozenIceEntity.class,EntityDataSerializers.FLOAT);
    private static EntityDataAccessor<Float> DEPTH = SynchedEntityData.defineId(FrozenIceEntity.class,EntityDataSerializers.FLOAT);




    public FrozenIceEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public FrozenIceEntity(Level level, LivingEntity target, int duration) {
        super(ModEntities.FROZEN_ICE.get(), level);
        this.frozenEntity = target;
        this.remainingTicks = duration;
        this.entityData.set(FROZEN_ENTITY,Optional.of(frozenEntity.getUUID()));
        AABB bb = target.getBoundingBox();
        float width = (float) (bb.maxX - bb.minX);
        float height = (float) (bb.maxY - bb.minY);
        float depth = (float) (bb.maxZ - bb.minZ);
        this.entityData.set(WIDTH,width);
        this.entityData.set(HEIGHT,height);
        this.entityData.set(DEPTH,depth);

        // 设置冰块的位置和大小
        AABB targetBounds = target.getBoundingBox();
        this.setBoundingBox(targetBounds.inflate(0.1)); // 稍微大一点包裹住实体
        this.setPos(target.getX(), target.getY(), target.getZ());
    }

   // public Entity getFrozenEntity(){
        //return frozenEntity;
        //return this.entityData.get(FROZEN_ENTITY).orElse(null);
        //return level().getEntity(this.entityData.get(FROZEN_ENTITY).orElse(null))
   // }

    @Override
    public void tick() {
        if(!level().isClientSide){
            super.tick();
            if (frozenEntity == null || !frozenEntity.isAlive()) {
                Spellweaver.getLOGGER().debug("[Spellweaver:FrozenIceEntity/tick()]frozenEntity不存在或死亡，移除冰块");
                discard();
                return;
            }

            // 跟随被冻结的实体
            setPos(frozenEntity.getX(), frozenEntity.getY(), frozenEntity.getZ());

            // 计时结束
            remainingTicks--;
            if (remainingTicks <= 0) {
                Spellweaver.getLOGGER().debug("[Spellweaver:FrozenIceEntity/tick()]frozenEntity冻结结束，移除冰块");
                discard();
            }
        }
    }
    public float getWidth(){
        return this.entityData.get(WIDTH);
    }
    public float getHeight(){
        return this.entityData.get(HEIGHT);
    }
    public float getDepth(){
        return this.entityData.get(DEPTH);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(FROZEN_ENTITY,Optional.empty());
        this.entityData.define(HEIGHT,0f);
        this.entityData.define(WIDTH,0f);
        this.entityData.define(DEPTH,0f);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // TODO 读取保存数据
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        // TODO 保存数据
    }
}
