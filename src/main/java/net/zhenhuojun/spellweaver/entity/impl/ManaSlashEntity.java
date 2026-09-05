package net.zhenhuojun.spellweaver.entity.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.zhenhuojun.spellweaver.damage_type.ModDamageTypes;
import net.zhenhuojun.spellweaver.entity.ModEntities;
import net.zhenhuojun.spellweaver.particle.ModParticle;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 魔法剑气：匀速直线飞行。
 * 不依赖父类的 xPower 加速度模型（会产生累积加速），而是每 tick 强制维持恒定速度，
 * 杜绝加速感、重力下坠、水阻等所有速度变化。
 */
public class ManaSlashEntity extends AbstractHurtingProjectile {
    private float damage;
    private float knockback;
    private int penetrateCount=1;
    private int life=600;

    // 匀速飞行的方向（单位向量）与速度大小。direction 仅服务端构造器设置；
    // 客户端首 tick 从同步包的 deltaMovement 反推。
    private Vec3 direction;
    private double speed;

    // 注册用的构造器
    public ManaSlashEntity(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
        super(entityType, level);
    }

    // 主动生成的构造器
    public ManaSlashEntity(Level level, LivingEntity owner, float damage, float knockback) {
        super(ModEntities.MANA_SLASH.get(), owner,
                0.0, 0.0, 0.0,
                level);
        this.damage = damage;
        this.knockback = knockback;
        this.setOwner(owner);
        Vec3 look = owner.getLookAngle();
        this.setPos(owner.getX() + look.x * 1.5,
                owner.getEyeY() - 0.1,
                owner.getZ() + look.z * 1.5);
        // 设置匀速飞行初速度（外部通过 setSpeed 调整大小）
        double baseSpeed = 1.0;
        this.direction = look;
        this.speed = baseSpeed;
        this.setDeltaMovement(this.direction.scale(this.speed));
        // 生成时按实际飞行方向设置朝向（渲染器后备用，避免依赖 deltaMovement 同步时序）
        double horizontal = Math.sqrt(look.x * look.x + look.z * look.z);
        this.setRot(
                (float) (Mth.atan2(look.z, look.x) * (180.0 / Math.PI)) - 90.0F,
                (float) (-(Mth.atan2(look.y, horizontal) * (180.0 / Math.PI)))
        );
    }

    public ManaSlashEntity(EntityType<? extends AbstractHurtingProjectile> entityType, Level level,
                           LivingEntity owner, float damage, float knockback,int penetrateCount,float speed) {
        super(entityType, owner, 0, 0, 0, level);
        this.damage = damage;
        this.knockback = knockback;
        this.penetrateCount=penetrateCount;
        this.setOwner(owner);

        // 从玩家眼前发出
        Vec3 look = owner.getLookAngle();
        this.setPos(owner.getX() + look.x * 1.5,
                owner.getEyeY() - 0.1,
                owner.getZ() + look.z * 1.5);


        //this.setDeltaMovement(look.scale(1.0));

        this.direction = look;
        this.speed = speed>2.5?2.5:speed;
        this.setDeltaMovement(this.direction.scale(this.speed));

        // 设置朝向（让模型指向运动方向）
        double horizontal = Math.sqrt(look.x * look.x + look.z * look.z);
        this.setRot(
                (float) (Mth.atan2(look.z, look.x) * (180.0 / Math.PI)) - 90.0F,
                (float) (-(Mth.atan2(look.y, horizontal) * (180.0 / Math.PI)))
        );
    }

    //@Override
    /*public void tick() {
        // 客户端首 tick：从同步包收到的 deltaMovement 反推方向，与服务端保持一致
        if (this.direction == null) {
            Vec3 motion = this.getDeltaMovement();
            double len = motion.length();
            if (len > 1.0E-6D) {
                this.speed = len;
                this.direction = motion.scale(1.0 / len);
            }
        }
        // 每 tick 强制恒定速度，杜绝父类加速度/重力/水阻导致的速度变化
        if (this.direction != null) {
            this.setDeltaMovement(this.direction.scale(this.speed));
        }
        //寿命
        this.life--;
        if(this.life<1){
            this.discard();
        }

        if (!this.level().isClientSide) {
            Entity owner = this.getOwner();
            // 获取与自身碰撞箱相交（略微膨胀）的实体，排除自己和主人
            List<Entity> entities = this.level().getEntities(this,
                    this.getBoundingBox().inflate(0.1),
                    e -> e != this && e != owner && e.isPickable());
            if(!entities.isEmpty()) {
                if(this.penetrateCount>0){
                    penetrateCount--;
                }else {
                    this.discard();
                }
            }
            for (Entity target : entities) {
                if (target instanceof LivingEntity) {
                    this.onHitEntity(new EntityHitResult(target));
                    if (!this.isAlive()) {
                        return;
                    }
                    //break;
                }
            }
        }

        super.tick();
    }

     */

    @Override
    public void tick() {
        if (this.direction == null) {
            Vec3 motion = this.getDeltaMovement();
            double len = motion.length();
            if (len > 1.0E-6D) {
                this.speed = len;
                this.direction = motion.scale(1.0 / len);
            }
        }

        // 检查是否应该继续存在
        Entity owner = this.getOwner();
        if (!this.level().isClientSide && (owner == null || owner.isRemoved()) || !this.level().hasChunkAt(this.blockPosition())) {
            this.discard();
            return;
        }

        // 保存旧位置
        Vec3 oldPos = this.position();

        // 移动实体
        Vec3 motion = this.getDeltaMovement();
        double newX = this.getX() + motion.x;
        double newY = this.getY() + motion.y;
        double newZ = this.getZ() + motion.z;
        this.setPos(newX, newY, newZ);

        // 更新朝向
        ProjectileUtil.rotateTowardsMovement(this, 0.2F);

        if (!this.level().isClientSide) {
            this.life--;
            if (this.life <= 0) {
                this.discard();
                return;
            }
        }
        if (!this.level().isClientSide) {
            // 方块碰撞：检查新位置的包围盒是否与实心方块重叠
            if (this.checkBlockCollision()) {
                this.discard();
                return;
            }

            // 构造扫掠盒：合并旧位置和新位置的包围盒
            AABB oldBox = new AABB(oldPos, oldPos).inflate(0.1);
            AABB newBox = this.getBoundingBox().inflate(0.1);
            AABB sweptBox = oldBox.minmax(newBox);

            List<Entity> entities = this.level().getEntities(
                    this,
                    sweptBox,
                    e -> e != this && e != owner && e.isPickable()
            );

            Set<Entity> hitThisTick = new HashSet<>();
            for (Entity target : entities) {
                if (!(target instanceof LivingEntity living)) continue;
                //无敌帧检查，防止浪费穿透
                if (living.invulnerableTime > 0) continue;
                // 每个实体每 tick 只能被命中一次
                if (hitThisTick.contains(target)) continue;
                // 造成伤害
                DamageSource source = new DamageSource(
                        this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                                .getHolderOrThrow(ModDamageTypes.MANA_SLASH),null,owner
                );
                living.hurt(source, this.damage);
                living.setDeltaMovement(this.direction.scale(this.knockback));
                hitThisTick.add(target);
                // 消耗穿透
                if (this.penetrateCount > 0) {
                    this.penetrateCount--;
                    if (this.penetrateCount == 0) {
                        this.discard();
                        return;
                    }
                } else {
                    this.discard();
                    return;
                }
            }
        }

        // 每 tick 强制恒定速度
        if (this.direction != null) {
            this.setDeltaMovement(this.direction.scale(this.speed));
        }
    }
    private boolean checkBlockCollision() {
        AABB box = this.getBoundingBox();
        for (BlockPos pos : BlockPos.betweenClosed(
                Mth.floor(box.minX), Mth.floor(box.minY), Mth.floor(box.minZ),
                Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ))) {
            BlockState state = this.level().getBlockState(pos);
            if (!state.getCollisionShape(this.level(), pos).isEmpty()) {
                return true;
            }
        }
        return false;
    }


    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide) {
            Entity target = result.getEntity();
            Entity owner = this.getOwner();
            if (target != owner && target instanceof LivingEntity living) {
                living.hurt(level().damageSources().magic(), damage);
                Vec3 knockDir = living.position().subtract(this.position()).normalize();
                living.setDeltaMovement(knockDir.scale(knockback));

                /*if(this.penetrateCount>0){
                    penetrateCount--;
                }else {
                    this.discard();
                }

                 */
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!level().isClientSide) {
           // if(this.penetrateCount>0){
               // penetrateCount--;
           // }else {
                this.discard();
           // }
           // this.discard();
        }
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected @NotNull ParticleOptions getTrailParticle() {
        return ModParticle.BLANK_PARTICLE.get();
    }

    // 当你觉得自己没用的时候不妨看看它⬇
    public void setSpeed(double speed) {
        this.speed = speed;
        if (this.direction != null) {
            this.setDeltaMovement(this.direction.scale(this.speed));
        }
    }
}
