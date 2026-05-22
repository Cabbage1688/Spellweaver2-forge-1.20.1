package net.zhenhuojun.spellweaver.entity.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

//魔法光源
public class MagicLightEntity extends Entity
{
    private int particleCooldown = 0;
    private BlockPos lightPos; // 记录光源方块的位置

    public MagicLightEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setInvulnerable(true);
    }

    // 设置光源位置的方法
    public void setLightPos(BlockPos pos) {
        this.lightPos = pos;
    }



    @Override
    public void tick() {
        super.tick();

        // 如果lightPos为null，尝试根据实体位置计算
        if (lightPos == null) {
            lightPos = this.blockPosition();
        }

        // 产生粒子效果
        /*if (this.particleCooldown <= 0) {
            this.spawnParticles();
            this.particleCooldown = 10 + this.random.nextInt(10); // 随机间隔
        } else {
            this.particleCooldown--;
        }

         */

        if(!this.level().isClientSide){
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.INSTANT_EFFECT,this.getX(),
                    this.getY()+0.1,this.getZ(),2,0,0,0,0);
        }

        // 轻微浮动效果，这个要改不然一直往上飘
        //this.setDeltaMovement(this.getDeltaMovement().add(0, 0.001, 0));
        //this.move(MoverType.SELF, this.getDeltaMovement());

        // 缓慢减速
        // this.setDeltaMovement(this.getDeltaMovement().scale(0.95));
    }

    // 移除光源方块的方法
    private void removeLightBlock() {
        if (!this.level().isClientSide && lightPos != null) {
            if(level().getBlockState(lightPos).is(Blocks.LIGHT)) {
                level().destroyBlock(lightPos, false);
            }
        }
    }

    private void spawnParticles() {
        if (this.level().isClientSide) {
            // 产生末地烛粒子效果
            for (int i = 0; i < 3; i++) {
                double xOffset = (this.random.nextDouble() - 0.5) * 0.5;
                double yOffset = (this.random.nextDouble() - 0.5) * 0.5;
                double zOffset = (this.random.nextDouble() - 0.5) * 0.5;
                this.level().addParticle(ParticleTypes.END_ROD,
                        this.getX() + xOffset,
                        this.getY() + yOffset,
                        this.getZ() + zOffset,
                        xOffset * 0.1,
                        yOffset * 0.1,
                        zOffset * 0.1);
            }
            // 添加光晕效果
            this.level().addParticle(ParticleTypes.GLOW,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    0, 0, 0);
        }
    }

    @Override
    protected void defineSynchedData() {
        // 不需要同步数据
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("LightX") && compound.contains("LightY") && compound.contains("LightZ")) {
            this.lightPos = new BlockPos(
                    compound.getInt("LightX"),
                    compound.getInt("LightY"),
                    compound.getInt("LightZ")
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        //compound.putInt("Lifespan", this.lifespan);
        if (lightPos != null) {
            compound.putInt("LightX", lightPos.getX());
            compound.putInt("LightY", lightPos.getY());
            compound.putInt("LightZ", lightPos.getZ());
        }
    }

    /*@Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

     */ @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }



    @Override
    public boolean isPickable() {
        return true; // 使实体可以被点击
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        // 左键点击时移除实体
        if (!this.level().isClientSide) {
            removeLightBlock(); // 移除光源方块
            super.discard();
            //被破坏时的粒子效果
            spawnBreakParticles();

            return true;

        }
        return false;
    }
    //破坏粒子效果
    private void spawnBreakParticles() {
        if (this.level().isClientSide) {
            for (int i = 0; i < 10; i++) {
                this.level().addParticle(ParticleTypes.END_ROD,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        (this.random.nextDouble() - 0.5) * 0.5,
                        (this.random.nextDouble() - 0.5) * 0.5,
                        (this.random.nextDouble() - 0.5) * 0.5);
            }
        }
    }

    // 设置实体的碰撞箱大小
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(0.5f, 0.5f);
    }
}