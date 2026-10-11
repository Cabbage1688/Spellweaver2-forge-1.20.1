package net.zhenhuojun.spellweaver.spell.node;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.capability.impl.mana.ManaUtil;
import net.zhenhuojun.spellweaver.entity.ModEntities;
import net.zhenhuojun.spellweaver.entity.impl.SpellEntity;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.PhotonEffectS2CPacket;
import net.zhenhuojun.spellweaver.spell.SpellContext;
import net.zhenhuojun.spellweaver.spell.SpellExecutionException;
import net.zhenhuojun.spellweaver.spell.util.EffectType;

import java.util.List;

/**
 * 法术实体节点：执行时从法术栈弹出魔力数值与坐标（两种栈序均支持：
 * 栈顶Double其下Vec3，或栈顶Vec3其下Double），向当前魔源收取等价魔力后，
 * 深拷贝内部子法术树生成法术实体，并把实体压入法术栈。
 */
public class SpellEntityNode implements Node {
    private final SequenceNode subSpell = new SequenceNode();
    //触发模式
    private SpellEntity.TriggerMode triggerMode = SpellEntity.TriggerMode.DELAY;
    //延时模式下的延迟tick数
    private int delayTicks = 20;
    //触发后是否继续飞行，false则原地停止
    private boolean keepFlying = false;

    @Override
    public NodeEnum getEnum() {
        return NodeEnum.SPELL_ENTITY;
    }

    @Override
    public String getType() {
        return "spell_entity";
    }

    public SequenceNode getSubSpell() {
        return subSpell;
    }


    public List<Node> getChildrenNodeList() {
        return subSpell.getChildrenNodeList();
    }

    public void addChildren(Node node) {
        subSpell.addChildren(node);
    }

    public void killChildren() {
        subSpell.killChildren();
    }

    public void clearChildren() {
        subSpell.clearChildren();
    }

    public SpellEntity.TriggerMode getTriggerMode() {
        return triggerMode;
    }

    public void setTriggerMode(SpellEntity.TriggerMode triggerMode) {
        if (triggerMode != null) {
            this.triggerMode = triggerMode;
        }
    }

    public int getDelayTicks() {
        return delayTicks;
    }

    public void setDelayTicks(int delayTicks) {
        this.delayTicks = Math.max(0, delayTicks);
    }

    public boolean isKeepFlying() {
        return keepFlying;
    }

    public void setKeepFlying(boolean keepFlying) {
        this.keepFlying = keepFlying;
    }

    /**
     * 执行节点：弹栈（兼容两种参数顺序）、向当前魔源收取等价魔力、深拷贝子树、生成法术实体、实体压栈。
     * 写法①坐标 数值 [本节点]：栈顶Double=魔力，其下Vec3=坐标
     * 写法②数值 坐标 [本节点]：栈顶Vec3=坐标，其下Double=魔力
     */
    public NodeResult execute(SpellContext context) {
        try {
            double mana;
            Vec3 pos;
            //按栈顶类型决定弹栈顺序
            if (context.isTop(Double.class)) {
                mana = context.pop(Double.class);
                pos = context.pop(Vec3.class);
            } else if (context.isTop(Vec3.class)) {
                pos = context.pop(Vec3.class);
                mana = context.pop(Double.class);
            } else {
                throw new SpellExecutionException(
                        Component.translatable("message.spellweaver.spell_entity_need_mana_and_pos").getString());
            }
            if (mana <= 0) {
                throw new SpellExecutionException(
                        Component.translatable("message.spellweaver.spell_entity_mana_positive").getString());
            }

            //向当前魔源（玩家/法杖/卷轴/机器等，玩家来源会顺带加经验）收取与实体预算等价的魔力，
            //魔力不足时扣费方法内部已按魔镜配置报错，这里直接FAULT且不生成实体
            if (!ManaUtil.subManaAndAddExpAndSendPacket(mana, context)) {
                return NodeResult.FAULT;
            }

            //深拷贝：每次执行生成独立的子树，循环节点中反复触发也不会共享运行状态
            SequenceNode copy = new SequenceNode();
            copy.deserializeNBT(subSpell.serializeNBT());

            //超载继承：子树直接继承父法术解析后的最终倍数（玩家当前超载值）
            if (context.rootNode != null) {
                copy.setOverloadMultiplier(context.rootNode.getOverloadMultiplier());
            }

            SpellEntity entity = new SpellEntity(ModEntities.SPELL_ENTITY.get(), context.level,
                    mana, copy, triggerMode, delayTicks, keepFlying);
            entity.setOwner(context.player);
            entity.setPos(pos.x, pos.y, pos.z);
            context.level.addFreshEntity(entity);
            ModMessage.sendToClients(new PhotonEffectS2CPacket(
                    ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID, "spell_entity3"),
                    context.level, entity, EffectType.INSTANT_DEAD_ENTITY_EFFECT));
            //实体压栈，后续符文可继续操作（如驱动）
            context.push(entity);
            return NodeResult.SUCCESS;
        } catch (SpellExecutionException e) {
            if (context.showErrorMessages && context.player != null) {
                context.player.sendSystemMessage(
                        Component.translatable("message.spellweaver.spell_entity_param_error", e.getMessage()));
            }
            Spellweaver.getLOGGER().debug("[Spellweaver:SpellEntityNode/execute]法术实体节点执行异常：{}", e.getMessage());
            return NodeResult.FAULT;
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", getType());
        tag.putString("triggerMode", triggerMode.getName());
        tag.putInt("delayTicks", delayTicks);
        tag.putBoolean("keepFlying", keepFlying);
        tag.put("subSpell", subSpell.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        subSpell.clearChildren();
        this.triggerMode = SpellEntity.TriggerMode.byName(tag.getString("triggerMode"));
        this.delayTicks = Math.max(0, tag.getInt("delayTicks"));
        this.keepFlying = tag.getBoolean("keepFlying");
        if (tag.contains("subSpell")) {
            subSpell.deserializeNBT(tag.getCompound("subSpell"));
        }
    }
}
