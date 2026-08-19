package net.zhenhuojun.spellweaver.entity.impl;

import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.zhenhuojun.spellweaver.capability.impl.mana.ManaSource;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.SpellListEntry;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.StoredSpell;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerManaOverloadProvider;
import net.zhenhuojun.spellweaver.entity.ai.MagicStarAi;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.MagicStarSyncS2CPacket;
import net.zhenhuojun.spellweaver.spell.SpellContext;
import net.zhenhuojun.spellweaver.spell.SpellTreeExecuteManager;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * 魔法之星实体：
 * 参考循声守卫的brain架构，支持四种模式（跟随/定点/待机/巡逻）
 * 使用主人的魔力施放法术，通过法术列表触发不同类型的法术
 */
public class MagicStarEntity extends PathfinderMob {
    private UUID ownerUUID;
    private ServerPlayer cachedOwner;
    private BlockPos patrolCenter;

    private List<SpellListEntry> attackSpells = new ArrayList<>();
    private List<SpellListEntry> shieldSpells = new ArrayList<>();
    private List<SpellListEntry> protectMasterSpells = new ArrayList<>();
    private List<SpellListEntry> routineSpells = new ArrayList<>();

    // 是否允许使用相应列表的法术
    private boolean masterPermitAttack = true;
    private boolean masterPermitShield = true;
    private boolean masterPermitProtectMaster = true;
    private boolean masterPermitRoutine = true;

    private ActMode actMode = ActMode.FOLLOW;

    // 法术施放触发标志（由传感器/事件设置，由CastSpellListBehavior消费）
    private boolean attackCastRequested;
    private boolean shieldCastRequested;
    private boolean protectMasterCastRequested;

    // 记录主人上次受伤时间戳，用于检测主人受伤
    private int lastOwnerHurtTimestamp;

    // 被禁用的法术UUID集合（按列表独立：0=攻击,1=自保,2=护主,3=日常）
    private Map<Integer, Set<UUID>> disabledSpells = new HashMap<>();

    // 视线遮挡检测：记录目标最后可见的gameTime
    private long lastTargetVisibleGameTime;

    public MagicStarEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        // 飞行移动控制：替换默认地面型MoveControl，10=最大转身速度，true=可悬停
        this.moveControl = new FlyingMoveControl(this, 10, true);
    }

    public MagicStarEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel,ServerPlayer player){
        this(pEntityType, pLevel);
        setOwner(player);
    }

    /**
     * 使用飞行寻路：让 navigation 能计算 3D 飞行路径而非地面路径。
     */
    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level pLevel) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, pLevel);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }
    @Override
    public boolean isNoGravity() {
        return true;
    }
    @Override
    protected boolean isFlapping() {
        return true;
    }

    /** 免疫摔伤 */
    @Override
    protected int calculateFallDamage(float fallDistance, float damageMultiplier) {
        return 0;
    }

    //TODO 数值先这么填着以后再细化平衡性调整
    public static AttributeSupplier.Builder createAttributes() {
        return MagicStarEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1024)
                .add(Attributes.ATTACK_DAMAGE, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ARMOR, 20.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.FLYING_SPEED,6);

    }

    public enum ActMode {
        FOLLOW,  // 跟随模式
        STOP,    // 定点模式
        SLEEP,   // 待机模式
        PATROL   // 巡逻模式
    }

    // ==================== Brain ====================

    @Override
    protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
        return MagicStarAi.makeBrain(this, pDynamic);
    }

    @Override
    public Brain<MagicStarEntity> getBrain() {
        return (Brain<MagicStarEntity>) super.getBrain();
    }

    @Override
    protected void customServerAiStep() {
        // 跟随模式：32格外传送优先级高于追击目标
        // 在 brain tick 之前执行，传送后清除目标避免追击行为干扰
        if (actMode == ActMode.FOLLOW) {
            ServerPlayer owner = getOwner();
            if (owner != null && this.distanceToSqr(owner) > 32 * 32) {
                this.teleportTo(owner.getX(), owner.getY(), owner.getZ());
                this.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                return; // 跳过本 tick 的 brain 处理
            }
        }

        ServerLevel serverLevel = (ServerLevel) this.level();
        serverLevel.getProfiler().push("magicStarBrain");
        this.getBrain().tick(serverLevel, this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
        MagicStarAi.updateActivity(this);

        // 锁定目标时让视线看向目标
        Optional<LivingEntity> targetOpt = this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        if (targetOpt.isPresent() && targetOpt.get().isAlive()) {
            LivingEntity target = targetOpt.get();
            // lookControl 用于客户端头部动画（逐步旋转）
            this.getLookControl().setLookAt(target, 90.0F, 90.0F);
            // 直接设置 yRot/xRot，确保 getLookAngle() 立即返回正确视线方向
            double dx = target.getX() - this.getX();
            double dy = target.getEyeY() - this.getEyeY();
            double dz = target.getZ() - this.getZ();
            double horizDist = Math.sqrt(dx * dx + dz * dz);
            this.setYRot((float)(Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F);
            this.setXRot((float)(-Math.atan2(dy, horizDist) * (180.0 / Math.PI)));
        }
    }
    //我依稀记得aiStep和customServerAiStep好像一个客户端一个服务端
    //但很神秘的是，不调super也会影响移动
    @Override
    public void aiStep(){
        super.aiStep();
        //待机模式没有粒子特效
        if(actMode!=ActMode.SLEEP){
            this.level().addParticle(ParticleTypes.GLOW, this.getRandomX(0.6D), this.getRandomY(), this.getRandomZ(0.6D), 0.0D, 0.0D, 0.0D);
        }
    }

    // ==================== 主人 ====================

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public ServerPlayer getOwner() {
        if (ownerUUID == null) return null;
        if (cachedOwner != null && cachedOwner.isAlive() && !cachedOwner.hasDisconnected()) {
            return cachedOwner;
        }
        if (level() instanceof ServerLevel serverLevel) {
            cachedOwner = serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
        }
        return cachedOwner;
    }

    public void setOwner(ServerPlayer owner) {
        this.ownerUUID = owner.getUUID();
        this.cachedOwner = owner;
    }

    // ==================== 模式 ====================

    public ActMode getActMode() {
        return actMode;
    }

    public void setActMode(ActMode mode) {
        // 进入巡逻模式时锁定当前位置为巡逻中心
        if (mode == ActMode.PATROL && this.actMode != ActMode.PATROL) {
            this.patrolCenter = this.blockPosition();
        }
        this.actMode = mode;
    }

    // ==================== 巡逻 ====================

    public BlockPos getPatrolCenter() {
        return patrolCenter;
    }

    // ==================== 法术列表 ====================

    public List<SpellListEntry> getAttackSpells() { return attackSpells; }
    public List<SpellListEntry> getShieldSpells() { return shieldSpells; }
    public List<SpellListEntry> getProtectMasterSpells() { return protectMasterSpells; }
    public List<SpellListEntry> getRoutineSpells() { return routineSpells; }

    public void setAttackSpells(List<SpellListEntry> spells) { this.attackSpells = spells != null ? spells : new ArrayList<>(); }
    public void setShieldSpells(List<SpellListEntry> spells) { this.shieldSpells = spells != null ? spells : new ArrayList<>(); }
    public void setProtectMasterSpells(List<SpellListEntry> spells) { this.protectMasterSpells = spells != null ? spells : new ArrayList<>(); }
    public void setRoutineSpells(List<SpellListEntry> spells) { this.routineSpells = spells != null ? spells : new ArrayList<>(); }

    // ==================== 主人许可 ====================

    public boolean isMasterPermitAttack() { return masterPermitAttack; }
    public boolean isMasterPermitShield() { return masterPermitShield; }
    public boolean isMasterPermitProtectMaster() { return masterPermitProtectMaster; }
    public boolean isMasterPermitRoutine() { return masterPermitRoutine; }

    public void setMasterPermitAttack(boolean v) { this.masterPermitAttack = v; }
    public void setMasterPermitShield(boolean v) { this.masterPermitShield = v; }
    public void setMasterPermitProtectMaster(boolean v) { this.masterPermitProtectMaster = v; }
    public void setMasterPermitRoutine(boolean v) { this.masterPermitRoutine = v; }

    // ==================== 法术施放触发 ====================

    public void requestAttackCast() { this.attackCastRequested = true; }
    public void clearAttackCastRequest() { this.attackCastRequested = false; }
    public boolean isAttackCastRequested() { return attackCastRequested; }

    public void requestShieldCast() { this.shieldCastRequested = true; }
    public void clearShieldCastRequest() { this.shieldCastRequested = false; }
    public boolean isShieldCastRequested() { return shieldCastRequested; }

    public void requestProtectMasterCast() { this.protectMasterCastRequested = true; }
    public void clearProtectMasterCastRequest() { this.protectMasterCastRequested = false; }
    public boolean isProtectMasterCastRequested() { return protectMasterCastRequested; }

    // ==================== 禁用法术（按列表独立） ====================

    public boolean isSpellDisabled(int listType, UUID spellId) {
        Set<UUID> set = disabledSpells.get(listType);
        return set != null && set.contains(spellId);
    }
    public void toggleSpellDisabled(int listType, UUID spellId) {
        Set<UUID> set = disabledSpells.computeIfAbsent(listType, k -> new HashSet<>());
        if (!set.add(spellId)) set.remove(spellId);
    }
    public Map<Integer, Set<UUID>> getDisabledSpellsMap() { return disabledSpells; }
    public Set<UUID> getDisabledSpells(int listType) {
        return disabledSpells.getOrDefault(listType, new HashSet<>());
    }

    // ==================== 视线遮挡检测 ====================

    public long getLastTargetVisibleGameTime() { return lastTargetVisibleGameTime; }
    public void setLastTargetVisibleGameTime(long time) { this.lastTargetVisibleGameTime = time; }

    // ==================== 法术施放 ====================

    /**
     * 施放一个法术，使用主人的魔力源
     * @param spell 要施放的法术
     * @param target 目标实体（可为null）
     */
    public void castSpell(StoredSpell spell, LivingEntity target) {
        castSpell(spell, target, -1);
    }

    /**
     * 施放一个法术，使用主人的魔力源
     * @param spell 要施放的法术
     * @param target 目标实体（可为null）
     * @param listType 法术列表类型（0=攻击,1=自保,2=护主,3=日常），用于自动压入额外数据
     */
    public void castSpell(StoredSpell spell, LivingEntity target, int listType) {
        ServerPlayer owner = getOwner();
        if (spell == null || owner == null) return;
        // 序列化再反序列化以获得法术树的全新副本（避免复用已有运行状态的节点）
        SequenceNode node = new SequenceNode();
        node.deserializeNBT(spell.getSequenceNode().serializeNBT());
        // 使用主人的魔力源
        SpellContext context = new SpellContext(level(), owner, ManaSource.PLAYER);
        ///给自我符文用的设置
        context.magicStarEntity=this;
        context.castByMagicStar=true;
        node.setContext(context);
        // 应用主人的超载倍数
        owner.getCapability(PlayerManaOverloadProvider.PLAYER_MANA_OVERLOAD).ifPresent(cap -> {
            if (cap.isEnabled()) {
                node.setOverloadMultiplier(cap.getCurrentMultiplier());
            } else {
                node.setOverloadMultiplier(1);
            }
        });
        // 根据列表类型自动压入额外数据
        if (listType == 3) {
            // 日常法术：压入施法时所处的坐标
            context.push(this.position());
        } else if (listType == 1) {
            // 自保法术：压入自己
            context.push(this);
        }
        // 将目标压入法术栈
        if (target != null) {
            context.push(target);
        }
        // 推入法术树管理器
        SpellTreeExecuteManager.getInstance().addSpellTree(node);
    }

    // ==================== 受伤处理 ====================

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (!level().isClientSide && result && masterPermitShield) {
            shieldCastRequested = true;
        }
        return result;
    }

    // ==================== 玩家右键交互 ====================

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            // 仅主人可打开GUI
            if (ownerUUID != null && ownerUUID.equals(serverPlayer.getUUID())) {
                syncToOwner(true);
                return InteractionResult.CONSUME;
            }
        }
        return super.mobInteract(player, hand);
    }

    /**
     * 将实体数据同步给主人客户端。
     * @param openGui true表示同时命令客户端打开MagicStarScreen
     */
    public void syncToOwner(boolean openGui) {
        ServerPlayer owner = getOwner();
        if (owner == null) return;
        CompoundTag spellsTag = new CompoundTag();
        spellsTag.put("AttackSpells", serializeSpellList(attackSpells));
        spellsTag.put("ShieldSpells", serializeSpellList(shieldSpells));
        spellsTag.put("ProtectMasterSpells", serializeSpellList(protectMasterSpells));
        spellsTag.put("RoutineSpells", serializeSpellList(routineSpells));
        // 同步禁用法术集合（按列表独立）
        CompoundTag disabledTag = new CompoundTag();
        for (Map.Entry<Integer, Set<UUID>> entry : disabledSpells.entrySet()) {
            net.minecraft.nbt.ListTag listTag = new net.minecraft.nbt.ListTag();
            for (UUID id : entry.getValue()) {
                listTag.add(net.minecraft.nbt.NbtUtils.createUUID(id));
            }
            disabledTag.put(String.valueOf(entry.getKey()), listTag);
        }
        spellsTag.put("DisabledSpells", disabledTag);
        ModMessage.sendToPlayer(
                new MagicStarSyncS2CPacket(getId(), actMode.ordinal(), spellsTag, openGui),
                owner
        );
    }

    /** 操作C2S包处理后调用，回推同步（不重新打开GUI） */
    public void syncToOwner() {
        syncToOwner(false);
    }

    // ==================== Tick ====================

    @Override
    public void tick() {
        if (!level().isClientSide) {
            // 先检测主人受伤（在 super.tick() 之前设置标志，
            // 确保 brain tick 时 checkExtraStartConditions 能检测到 protectMasterCastRequested）
            ServerPlayer owner = getOwner();
            if (owner != null && owner.isAlive()) {
                int currentHurtTime = owner.getLastHurtByMobTimestamp();
                if (currentHurtTime > lastOwnerHurtTimestamp && owner.tickCount - currentHurtTime < 100) {
                    lastOwnerHurtTimestamp = currentHurtTime;
                    if (masterPermitProtectMaster) {
                        protectMasterCastRequested = true;
                    }
                }
            }
        }
        super.tick();
    }

    // ==================== NBT存取 ====================

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (ownerUUID != null) {
            compound.putUUID("OwnerUUID", ownerUUID);
        }
        compound.putInt("ActMode", actMode.ordinal());
        if (patrolCenter != null) {
            compound.putLong("PatrolCenter", patrolCenter.asLong());
        }
        compound.putBoolean("PermitAttack", masterPermitAttack);
        compound.putBoolean("PermitShield", masterPermitShield);
        compound.putBoolean("PermitProtectMaster", masterPermitProtectMaster);
        compound.putBoolean("PermitRoutine", masterPermitRoutine);
        compound.put("AttackSpells", serializeSpellList(attackSpells));
        compound.put("ShieldSpells", serializeSpellList(shieldSpells));
        compound.put("ProtectMasterSpells", serializeSpellList(protectMasterSpells));
        compound.put("RoutineSpells", serializeSpellList(routineSpells));
        // 序列化禁用法术集合（按列表独立）
        CompoundTag disabledTag = new CompoundTag();
        for (Map.Entry<Integer, Set<UUID>> entry : disabledSpells.entrySet()) {
            net.minecraft.nbt.ListTag listTag = new net.minecraft.nbt.ListTag();
            for (UUID id : entry.getValue()) {
                listTag.add(net.minecraft.nbt.NbtUtils.createUUID(id));
            }
            disabledTag.put(String.valueOf(entry.getKey()), listTag);
        }
        compound.put("DisabledSpells", disabledTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("OwnerUUID")) {
            ownerUUID = compound.getUUID("OwnerUUID");
        }
        if (compound.contains("ActMode")) {
            int ordinal = compound.getInt("ActMode");
            ActMode[] modes = ActMode.values();
            if (ordinal >= 0 && ordinal < modes.length) {
                actMode = modes[ordinal];
            }
        }
        if (compound.contains("PatrolCenter")) {
            patrolCenter = BlockPos.of(compound.getLong("PatrolCenter"));
        }
        masterPermitAttack = compound.getBoolean("PermitAttack");
        masterPermitShield = compound.getBoolean("PermitShield");
        masterPermitProtectMaster = compound.getBoolean("PermitProtectMaster");
        masterPermitRoutine = compound.getBoolean("PermitRoutine");
        attackSpells = deserializeSpellList(compound.getList("AttackSpells", Tag.TAG_COMPOUND));
        shieldSpells = deserializeSpellList(compound.getList("ShieldSpells", Tag.TAG_COMPOUND));
        protectMasterSpells = deserializeSpellList(compound.getList("ProtectMasterSpells", Tag.TAG_COMPOUND));
        routineSpells = deserializeSpellList(compound.getList("RoutineSpells", Tag.TAG_COMPOUND));
        // 反序列化禁用法术集合（按列表独立）
        disabledSpells.clear();
        CompoundTag disabledTag = compound.getCompound("DisabledSpells");
        for (String key : disabledTag.getAllKeys()) {
            int listType = Integer.parseInt(key);
            net.minecraft.nbt.ListTag listTag = disabledTag.getList(key, Tag.TAG_INT_ARRAY);
            Set<UUID> set = new HashSet<>();
            for (int i = 0; i < listTag.size(); i++) {
                set.add(net.minecraft.nbt.NbtUtils.loadUUID(listTag.get(i)));
            }
            disabledSpells.put(listType, set);
        }
    }

    private ListTag serializeSpellList(List<SpellListEntry> entries) {
        ListTag tag = new ListTag();
        for (SpellListEntry entry : entries) {
            tag.add(entry.serialize());
        }
        return tag;
    }

    private List<SpellListEntry> deserializeSpellList(ListTag tag) {
        List<SpellListEntry> entries = new ArrayList<>();
        for (int i = 0; i < tag.size(); i++) {
            entries.add(SpellListEntry.deserialize(tag.getCompound(i)));
        }
        return entries;
    }
}
