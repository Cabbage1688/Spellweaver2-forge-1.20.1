package net.zhenhuojun.spellweaver.entity.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.ManaBallEffectS2CPacket;
import net.zhenhuojun.spellweaver.spell.RuneRegister;
import net.zhenhuojun.spellweaver.spell.SpellContext;
import net.zhenhuojun.spellweaver.spell.util.RunesExecuteMethod;
import org.jetbrains.annotations.NotNull;

/**
 * 致阅读此代码的人：
 * 如果你想写一个飞弹类的实体，我更推荐你继承AbstractHurtingProjectile而不是Projectile
 * 因为AbstractHurtingProjectile的tick方法自带速度处理，而Projectile没有
 * 也就是说如果你继承Projectile，你需要自己实现运动逻辑
 * 相比之下，AbstractHurtingProjectile的燃烧和黑烟拖尾可以很容易地处理掉
 */
public class ManaBall extends AbstractHurtingProjectile {
    private RuneRegister runeRegister;
    private Player player;
    private int time=600;
    private Entity entity;
    private SpellContext previousContext;


    public ManaBall(EntityType<? extends AbstractHurtingProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ManaBall(RuneRegister runeRegister,Player player,EntityType<? extends AbstractHurtingProjectile> pEntityType, Level pLevel,SpellContext context){
        super(pEntityType, pLevel);
        this.runeRegister=runeRegister;
        this.player=player;
        this.setOwner(player);
        this.previousContext=context;
    }

    public SpellContext getPreviousContext(){
        return this.previousContext;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setRuneRegister(RuneRegister runeRegister) {
        this.runeRegister = runeRegister;
    }

    /**
     * 一个推测：
     * 如果命中实体，HitResult.getResult()获取的坐标的y是实体位置的y，而不是命中的位置的y
     *
     */
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            if(runeRegister!=null&&runeRegister.getSpellList()!=null){
                //防崩
                if(this.previousContext==null) return;
                //执行回调
                RunesExecuteMethod.simpleSpellLogic(runeRegister.getSpellList(),level(),player,result.getLocation(),entity,previousContext);
                ModMessage.sendToClients(new ManaBallEffectS2CPacket(this.xo,this.yo,this.zo,0xE9FAFF));
            }
            this.discard();
        }
    }

    @Override
    public boolean canHitEntity(Entity target) {
        // 忽略所有者和魔法飞弹
        if (target == getOwner()||target instanceof ManaBall) {
            return false;
        }
        return super.canHitEntity(target);
    }
    //2026.5.26增强，魔法飞弹命中后会重置无敌帧数
    @Override
    public void onHitEntity(@NotNull EntityHitResult result){
        super.onHitEntity(result);
        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity entity) {
            entity.invulnerableTime = 0;
            entity.hurtTime = 0;
            this.entity=entity;
            Spellweaver.getLOGGER().debug("[Spellweaver/ManaBall]魔法飞弹命中实体并存储，实体{}",entity);
        }
    }


    @Override
    protected void defineSynchedData() {
    }
    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        // 返回 null 以完全禁用轨迹粒子
        return ParticleTypes.INSTANT_EFFECT;
    }

    public void tick(){
        time--;
        if(time<=0){
            this.discard();
        }
        super.tick();
    }
    //保存自定义实体的数据到游戏世界文件
    /*public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        //byte 型爆炸威力
        compound.putInt("ExplosionPower", this.explosionPower);
        //伤害数值写大点
        compound.putDouble("Damage",this.damage);
        compound.putBoolean("WhetherBreakBlock",this.whetherBreakBlock);
    }
    //读取之前保存的数据，在实体被重新加载时恢复它的状态。
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        //99是一个标签ID，它指定了要检查的数据类型。
        // 它对应的是 Tag.TAG_ANY_NUMERIC，意思是“任何数字类型”（Byte, Short, Int, Long, Float, Double）。
        if (compound.contains("ExplosionPower", 99)&&compound.contains("Damage",99)) {
            this.explosionPower = compound.getInt("ExplosionPower");
            this.damage=compound.getDouble("Damage");
            this.whetherBreakBlock=compound.getBoolean("WhetherBreakBlock");
        }

    }

     */
}
