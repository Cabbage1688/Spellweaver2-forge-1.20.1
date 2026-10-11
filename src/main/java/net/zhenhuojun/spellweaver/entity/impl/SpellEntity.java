package net.zhenhuojun.spellweaver.entity.impl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.zhenhuojun.spellweaver.capability.impl.mana.ManaSource;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.spell.SpellContext;
import net.zhenhuojun.spellweaver.spell.SpellTreeExecuteManager;
import net.zhenhuojun.spellweaver.spell.node.NodeResult;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.UUID;

/**
 * 法术实体：携带一棵完整子法术树与独立法力预算的抛射物。
 * 触发模式：
 * - DELAY：生成后继续飞行（速度由外部符文设置），倒计时结束触发子法术
 * - HIT：飞行直到命中实体或方块时触发子法术
 * 触发后按keepFlying决定原地停止或继续飞行；子法术结束、法力耗尽或主人离线时实体消散，
 * 消散时同步取消仍在运行的子法术。
 */
public class SpellEntity extends Projectile {
    public enum TriggerMode {
        DELAY("delay"),
        HIT("hit");

        private final String name;

        TriggerMode(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static TriggerMode byName(String name) {
            for (TriggerMode mode : values()) {
                if (mode.name.equals(name)) {
                    return mode;
                }
            }
            return DELAY;
        }
    }

    private static final EntityDataAccessor<Boolean> DATA_TRIGGERED =
            SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_KEEP_FLYING =
            SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.BOOLEAN);
    //触发时的服务端冻结坐标：与DATA_TRIGGERED同包同步，供客户端吸附，消除触发同步延迟导致的滑行偏差
    private static final EntityDataAccessor<Vector3f> DATA_TRIGGER_POS =
            SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.VECTOR3);

    private double mana;
    private double subMana;//每tick自然流失的法力，防止实体永久存在
    private SequenceNode sequenceNode;
    private TriggerMode triggerMode = TriggerMode.DELAY;
    private int delayTicks = 20;
    private boolean keepFlying = false;
    //子法术是否已投入执行（运行时标记，不直接存档）
    private boolean spellCasted = false;
    //已投入执行的子法术树UUID（存档）：区块卸载无法走discard，重载重投时凭此驱逐可能残留的旧树
    @Nullable
    private UUID castTreeUuid = null;

    public SpellEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public SpellEntity(EntityType<? extends Projectile> pEntityType, Level pLevel, double mana,
                       SequenceNode sequenceNode, TriggerMode triggerMode, int delayTicks, boolean keepFlying) {
        super(pEntityType, pLevel);
        this.mana = mana;
        this.subMana = 0.0005 * mana;
        this.sequenceNode = sequenceNode;
        this.triggerMode = triggerMode;
        this.delayTicks = Math.max(0, delayTicks);
        this.keepFlying = keepFlying;
        this.entityData.set(DATA_KEEP_FLYING, keepFlying);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_TRIGGERED, false);
        this.entityData.define(DATA_KEEP_FLYING, false);
        this.entityData.define(DATA_TRIGGER_POS, new Vector3f());
    }

    public boolean isTriggered() {
        return this.entityData.get(DATA_TRIGGERED);
    }

    private void setTriggered(boolean triggered) {
        this.entityData.set(DATA_TRIGGERED, triggered);
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            //自然流失
            if (mana > 0) {
                mana = mana - subMana;
            } else {
                this.discard();
                return;
            }
            Entity owner = this.getOwner();
            if ((owner != null && owner.isRemoved()) || !this.level().hasChunkAt(this.blockPosition())) {
                this.discard();
                return;
            }
            //区块重载后，已触发但子法术尚未恢复执行的，等待主人加载后重新投入
            if (isTriggered() && !spellCasted && owner instanceof ServerPlayer) {
                castSpell(this.position(), null);
            }
            if (!isTriggered()) {
                if (triggerMode == TriggerMode.DELAY) {
                    if (delayTicks > 0) {
                        delayTicks--;
                    } else {
                        trigger(this.position(), null);
                    }
                } else if (triggerMode == TriggerMode.HIT) {
                    HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
                    if (hitResult.getType() != HitResult.Type.MISS
                            && !ForgeEventFactory.onProjectileImpact(this, hitResult)) {
                        this.onHit(hitResult);
                    }
                }
            }
        }

        super.tick();

        //触发后配置为原地停止的，作为固定施法源存在：
        //客户端每tick吸附到同步来的服务端冻结坐标，彻底消除触发延迟造成的漂移
        boolean frozen = this.entityData.get(DATA_TRIGGERED) && !this.entityData.get(DATA_KEEP_FLYING);
        if (frozen) {
            Vector3f frozenPos = this.entityData.get(DATA_TRIGGER_POS);
            this.setDeltaMovement(Vec3.ZERO);
            this.absMoveTo(frozenPos.x(), frozenPos.y(), frozenPos.z());
        } else {
            this.checkInsideBlocks();
            Vec3 vec3 = this.getDeltaMovement();
            this.setPos(this.getX() + vec3.x, this.getY() + vec3.y, this.getZ() + vec3.z);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide && !isTriggered() && triggerMode == TriggerMode.HIT) {
            Entity hitEntity = (result instanceof EntityHitResult entityHitResult) ? entityHitResult.getEntity() : null;
            trigger(result.getLocation(), hitEntity);
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        //忽略主人、其他法术实体以及同主人的魔法之星
        if (target == getOwner() || target instanceof SpellEntity) {
            return false;
        }
        if (target instanceof MagicStarEntity magicStarEntity && magicStarEntity.getOwner() == getOwner()) {
            return false;
        }
        return super.canHitEntity(target);
    }

    /**
     * 触发子法术，只会生效一次。
     */
    private void trigger(Vec3 triggerPos, @Nullable Entity hitEntity) {
        if (isTriggered()) {
            return;
        }
        setTriggered(true);
        //记录服务端冻结坐标并同步：触发标记有约1tick的同步延迟，客户端会多滑行1tick，
        //之后原版ServerEntity每60tick强制重发的位置包会把这段偏差突然拉回，表现为停止后坐标莫名回移。
        this.entityData.set(DATA_TRIGGER_POS, new Vector3f((float) this.getX(), (float) this.getY(), (float) this.getZ()));
        castSpell(triggerPos, hitEntity);
        if (!keepFlying) {
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    /**
     * 以法术实体自身为魔源施放内部子法术。
     * 触发坐标压入法术栈，如果命中的实体存入context，与魔法飞弹一致的缓存机制
     */
    public void castSpell(Vec3 triggerPos, @Nullable Entity hitEntity) {
        if (spellCasted || this.sequenceNode == null) {
            return;
        }
        if (!(getOwner() instanceof ServerPlayer owner)) {
            return;
        }
        spellCasted = true;
        SpellContext context = new SpellContext(level(), owner, ManaSource.SPELL_ENTITY);
        context.spellEntity = this;
        context.showErrorMessages = owner.getOffhandItem().is(ModItems.MAGICIAN_MIRROR.get())
                || owner.getMainHandItem().is(ModItems.MAGICIAN_MIRROR.get());
        context.push(triggerPos);
        if (hitEntity != null) {
            context.entity = hitEntity;
        }
        //子法术自然执行完毕后，实体立即消散
        context.setOnComplete(state -> {
            if (state != NodeResult.RUNNING && !this.isRemoved()) {
                this.discard();
            }
        });
        this.sequenceNode.setContext(context);
        //区块重载后重新投入时，反序列化出的是新UUID的子树，先取消卸载前可能残留的旧树
        if (this.castTreeUuid != null && !this.castTreeUuid.equals(this.sequenceNode.getUuid())) {
            SpellTreeExecuteManager.getInstance().cancelSpellTree(this.castTreeUuid);
        }
        SpellTreeExecuteManager.getInstance().addSpellTree(this.sequenceNode);
        this.castTreeUuid = this.sequenceNode.getUuid();
    }

    /**
     * 法力消耗入口。法力不足时实体消散
     */
    public boolean costMana(double cost) {
        if (mana >= cost) {
            mana = mana - cost;
            return true;
        } else {
            this.discard();
            return false;
        }
    }

    public void setMana(double mana) {
        this.mana = mana;
    }

    public double getMana() {
        return mana;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        this.mana = pCompound.getDouble("mana");
        this.subMana = pCompound.getDouble("sub_mana");
        this.delayTicks = Math.max(0, pCompound.getInt("delay_ticks"));
        this.keepFlying = pCompound.getBoolean("keep_flying");
        this.triggerMode = TriggerMode.byName(pCompound.getString("trigger_mode"));
        if (pCompound.contains("spell")) {
            SequenceNode node = new SequenceNode();
            node.deserializeNBT(pCompound.getCompound("spell"));
            this.sequenceNode = node;
        }
        this.entityData.set(DATA_KEEP_FLYING, keepFlying);
        this.entityData.set(DATA_TRIGGERED, pCompound.getBoolean("triggered"));
        if (isTriggered()) {
            //区块重载后恢复冻结坐标，避免客户端吸附到未初始化的默认值
            Vector3f frozenPos = pCompound.contains("trigger_pos_x")
                    ? new Vector3f(pCompound.getFloat("trigger_pos_x"), pCompound.getFloat("trigger_pos_y"), pCompound.getFloat("trigger_pos_z"))
                    : new Vector3f((float) getX(), (float) getY(), (float) getZ());
            this.entityData.set(DATA_TRIGGER_POS, frozenPos);
        }
        this.castTreeUuid = pCompound.hasUUID("spell_tree_uuid") ? pCompound.getUUID("spell_tree_uuid") : null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putDouble("mana", mana);
        pCompound.putDouble("sub_mana", subMana);
        pCompound.putInt("delay_ticks", delayTicks);
        pCompound.putBoolean("keep_flying", keepFlying);
        pCompound.putString("trigger_mode", triggerMode.getName());
        pCompound.putBoolean("triggered", isTriggered());
        if (sequenceNode != null) {
            pCompound.put("spell", sequenceNode.serializeNBT());
        }
        if (castTreeUuid != null) {
            pCompound.putUUID("spell_tree_uuid", castTreeUuid);
        }
        if (isTriggered()) {
            Vector3f frozenPos = this.entityData.get(DATA_TRIGGER_POS);
            pCompound.putFloat("trigger_pos_x", frozenPos.x());
            pCompound.putFloat("trigger_pos_y", frozenPos.y());
            pCompound.putFloat("trigger_pos_z", frozenPos.z());
        }
    }
}
