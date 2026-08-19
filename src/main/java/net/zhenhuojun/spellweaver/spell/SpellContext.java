package net.zhenhuojun.spellweaver.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.capability.impl.mana.ManaSource;
import net.zhenhuojun.spellweaver.entity.impl.MagicStarEntity;
import net.zhenhuojun.spellweaver.spell.util.SlotReference;
import net.zhenhuojun.spellweaver.spell.node.NodeResult;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class SpellContext {
    public final Deque<Object> stack = new ArrayDeque<>();//法术栈,存储法术运行时产生的数据
    public final Level level;
    public Player player;
    ///这个改动是为了支持魔法之星正确使用"自我"符文，而不是向法术栈压入主人
    public MagicStarEntity magicStarEntity;
    public boolean castByMagicStar;
    public ManaSource manaSource=ManaSource.PLAYER;

    public Map<String, Object> variables = new HashMap<>();//用于“存储变量”符文创建键值对

    private BlockPos machinePos;
    private BlockPos pedestalPos;
    private SlotReference manaBottleSlot;

    public int jumpTarget=-1;//用于跳转

    public Entity entity;
    //是否展示法术报错，当副手装备魔法师之镜时展示
    public boolean showErrorMessages = false;

    @Nullable
    private Consumer<NodeResult> onComplete;

    public SpellContext(Level level,Player player){
        this.level=level;
        this.player=player;
        this.castByMagicStar=false;
    }

    public SpellContext(Level level,Player player,ManaSource manaSource){
        this.level=level;
        this.player=player;
        this.manaSource=manaSource;
        this.castByMagicStar=false;
    }
   //这个专用于机器
    public SpellContext(Level level,Player player,ManaSource manaSource,BlockPos machinePos){
        this.level=level;
        this.player=player;
        this.manaSource=manaSource;
        this.machinePos=machinePos;
        this.castByMagicStar=false;
    }

    public BlockPos getMachinePos() {
        return machinePos;
    }

    public void setMachinePos(BlockPos machinePos){
        this.machinePos=machinePos;
    }

    public BlockPos getPedestalPos() {
        return pedestalPos;
    }

    public void setPedestalPos(BlockPos pedestalPos) {
        this.pedestalPos = pedestalPos;
    }

    public SlotReference getManaBottleSlot() {
        return manaBottleSlot;
    }

    public void setManaBottleSlot(SlotReference manaBottleSlot) {
        this.manaBottleSlot = manaBottleSlot;
    }

    public void setOnComplete(@Nullable Consumer<NodeResult> callback) {
        this.onComplete = callback;
    }

    public void notifyComplete(NodeResult state) {
        if (onComplete != null) {
            onComplete.accept(state);
        }
    }

    //弹出栈顶元素
    public <T> T pop(Class<T> type) throws SpellExecutionException {
        if (stack.isEmpty()) {
            //throw new SpellExecutionException("栈为空，无法弹出元素");
            throw new SpellExecutionException(
                    Component.translatable("message.spellweaver.stack_empty_pop").getString()
            );
        }

        Object obj = stack.pop();
        if (type.isInstance(obj)) {
            return type.cast(obj);
        }

        //throw new SpellExecutionException("类型不匹配。期望 " +
               // type.getSimpleName() + " 但得到 " + obj.getClass().getSimpleName());

        throw new SpellExecutionException(
                Component.translatable("message.spellweaver.type_mismatch",
                        type.getSimpleName(),
                        obj.getClass().getSimpleName()).getString()
        );
    }
    //查看栈顶元素
   /* public <T> T peek(Class<T> type) throws SpellExecutionException {
        if (stack.isEmpty()) {
            throw new SpellExecutionException("栈为空，无法查看元素");
        }

        Object obj = stack.peek();
        if (type.isInstance(obj)) {
            return type.cast(obj);
        }

        throw new SpellExecutionException("类型不匹配。期望 " +
                type.getSimpleName() + " 但得到 " + obj.getClass().getSimpleName());
    }

    */
    //向栈压入元素
    public void push(Object value) {
        if (stack == null) {
            Spellweaver.getLOGGER().error("[Spellweaver_SpellContext]Stack is null in SpellContext.push! This should not happen.");
            return;
        }
        // 添加空值检查
        if (value == null) {
            Spellweaver.getLOGGER().warn("[Spellweaver_SpellContext]Attempted to push null value onto the stack. This may indicate a problem in spell execution.");
            return; // 不压入null值，避免崩溃
        }
        stack.push(value);
    }
    // 辅助方法：检查栈顶类型
    public boolean isTop(Class<?> type) {
        return !stack.isEmpty() && type.isInstance(stack.peek());
    }

    public void setManaSource(ManaSource manaSource) {
        this.manaSource = manaSource;
    }
}
