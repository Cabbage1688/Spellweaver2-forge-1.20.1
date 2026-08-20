package net.zhenhuojun.spellweaver.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.SpellListEntry;
import net.zhenhuojun.spellweaver.entity.impl.MagicStarEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 通用法术施放行为：达成触发条件后，逐tick遍历列表条目。
 * - 法术条目：推入法术树管理器施放
 * - 延迟条目：等待指定tick数后继续
 * 遍历完毕后行为自动停止，等待下次触发条件达成。
 * 遍历时自动跳过被禁用的条目（entity.isSpellDisabled(uuid)）。
 *
 * 可选冷却模式（firstCastDelay/intervalDelay > 0 时启用）：
 * - 有目标时：发现新目标后等待 firstCastDelay tick 才首次施法，目标切换重置首次延迟
 * - 无目标时（定时模式）：首次等待 firstCastDelay tick，后续等待 intervalDelay tick
 */
public class CastSpellListBehavior extends Behavior<MagicStarEntity> {
    private final Function<MagicStarEntity, List<SpellListEntry>> spellListGetter;
    private final Predicate<MagicStarEntity> triggerPredicate;
    private final Function<MagicStarEntity, LivingEntity> targetGetter;
    private final Consumer<MagicStarEntity> triggerAcknowledge;
    private final int firstCastDelay;
    private final int intervalDelay;
    private final int listType; // 对应的法术列表类型（0=攻击,1=自保,2=护主,3=日常），用于禁用检查

    private int spellIndex;
    private UUID trackedTargetUUID;
    private long targetAcquiredGameTime;
    private long lastCastFinishGameTime;
    private boolean hasCastSinceTargetAcquired;
    private boolean initialized; // 定时模式首次初始化标记
    private long delayStartGameTime = -1; // 延迟条目计时起点

    /** 原有构造函数（无冷却，保持兼容） */
    public CastSpellListBehavior(
            Map<MemoryModuleType<?>, MemoryStatus> entryCondition,
            Function<MagicStarEntity, List<SpellListEntry>> spellListGetter,
            Predicate<MagicStarEntity> triggerPredicate,
            Function<MagicStarEntity, LivingEntity> targetGetter,
            Consumer<MagicStarEntity> triggerAcknowledge,
            int listType) {
        this(entryCondition, spellListGetter, triggerPredicate, targetGetter, triggerAcknowledge, 0, 0, listType);
    }

    /** 带冷却的构造函数 */
    public CastSpellListBehavior(
            Map<MemoryModuleType<?>, MemoryStatus> entryCondition,
            Function<MagicStarEntity, List<SpellListEntry>> spellListGetter,
            Predicate<MagicStarEntity> triggerPredicate,
            Function<MagicStarEntity, LivingEntity> targetGetter,
            Consumer<MagicStarEntity> triggerAcknowledge,
            int firstCastDelay,
            int intervalDelay,
            int listType) {
        super(entryCondition);
        this.spellListGetter = spellListGetter;
        this.triggerPredicate = triggerPredicate;
        this.targetGetter = targetGetter;
        this.triggerAcknowledge = triggerAcknowledge;
        this.firstCastDelay = firstCastDelay;
        this.intervalDelay = intervalDelay;
        this.listType = listType;
    }

    /** 检查列表中是否有至少一个未禁用的法术条目 */
    private boolean hasAnyEnabledSpell(MagicStarEntity entity) {
        List<SpellListEntry> entries = spellListGetter.apply(entity);
        if (entries == null || entries.isEmpty()) return false;
        for (SpellListEntry entry : entries) {
            if (entry.isSpell() && !entity.isSpellDisabled(listType, entry.getId())) return true;
        }
        return false;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MagicStarEntity entity) {
        if (!hasAnyEnabledSpell(entity)) return false;
        if (!triggerPredicate.test(entity)) return false;

        // 无冷却模式，直接触发
        if (firstCastDelay == 0 && intervalDelay == 0) {
            return true;
        }

        long gameTime = level.getGameTime();
        LivingEntity target = targetGetter.apply(entity);

        if (target == null) {
            // 定时模式（无目标）：不跟踪目标UUID，只看时间
            if (!initialized) {
                initialized = true;
                targetAcquiredGameTime = gameTime;
                hasCastSinceTargetAcquired = false;
            }
            if (!hasCastSinceTargetAcquired) {
                return gameTime - targetAcquiredGameTime >= firstCastDelay;
            } else {
                return gameTime - lastCastFinishGameTime >= intervalDelay;
            }
        }

        // 冷却模式：基于目标UUID跟踪
        UUID targetUUID = target.getUUID();

        // 检测目标切换：新目标重置首次延迟
        if (trackedTargetUUID == null || !trackedTargetUUID.equals(targetUUID)) {
            trackedTargetUUID = targetUUID;
            targetAcquiredGameTime = gameTime;
            hasCastSinceTargetAcquired = false;
        }

        if (!hasCastSinceTargetAcquired) {
            return gameTime - targetAcquiredGameTime >= firstCastDelay;
        } else {
            return gameTime - lastCastFinishGameTime >= intervalDelay;
        }
    }

    @Override
    protected void start(ServerLevel level, MagicStarEntity entity, long gameTime) {
        spellIndex = 0;
        delayStartGameTime = -1;
        triggerAcknowledge.accept(entity);
    }

    @Override
    protected void tick(ServerLevel level, MagicStarEntity entity, long gameTime) {
        List<SpellListEntry> entries = spellListGetter.apply(entity);
        if (entries == null) return;
        // 跳过被禁用的条目
        while (spellIndex < entries.size() && entity.isSpellDisabled(listType, entries.get(spellIndex).getId())) {
            spellIndex++;
        }
        if (spellIndex >= entries.size()) return;

        SpellListEntry entry = entries.get(spellIndex);
        if (entry.isDelay()) {
            // 延迟条目：等待指定tick后继续
            if (delayStartGameTime < 0) {
                delayStartGameTime = gameTime;
            }
            if (gameTime - delayStartGameTime >= entry.getDelayTicks()) {
                delayStartGameTime = -1;
                spellIndex++;
            }
            return;
        }

        // 法术条目：施放
        LivingEntity target = targetGetter.apply(entity);
        entity.castSpell(entry.getSpell(), target, listType);
        spellIndex++;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MagicStarEntity entity, long gameTime) {
        List<SpellListEntry> entries = spellListGetter.apply(entity);
        if (entries == null) return false;
        // 检查是否还有未禁用的法术条目可施放
        for (int i = spellIndex; i < entries.size(); i++) {
            SpellListEntry entry = entries.get(i);
            if (entry.isSpell() && !entity.isSpellDisabled(listType, entry.getId())) return true;
        }
        return false;
    }

    @Override
    protected void stop(ServerLevel level, MagicStarEntity entity, long gameTime) {
        lastCastFinishGameTime = gameTime;
        hasCastSinceTargetAcquired = true;
        delayStartGameTime = -1;
    }
}
