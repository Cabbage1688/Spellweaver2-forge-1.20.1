package net.zhenhuojun.spellweaver.spell.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.block.custom.SpellMachineBlockEntity;
import net.zhenhuojun.spellweaver.capability.impl.mana.ManaSource;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerManaOverloadProvider;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerManaProvider;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.spell.*;
import net.zhenhuojun.spellweaver.spell.node.NodeResult;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class RunesExecuteMethod {

    //context直接交给SequenceNode存储，每次修改后都同步修改
    public static void spellLogic(SequenceNode sequenceNode, Level level, Player player){
        SpellContext context=new SpellContext(level,player);
        context.showErrorMessages = player.getOffhandItem().is(ModItems.MAGICIAN_MIRROR.get()) || player.getMainHandItem().is(ModItems.MAGICIAN_MIRROR.get());
        sequenceNode.setContext(context);
        player.getCapability(PlayerManaOverloadProvider.PLAYER_MANA_OVERLOAD).ifPresent(cap -> {
            Spellweaver.getLOGGER().debug("[SPELLWEAVER:RunesExecuteMethod/spellLogic方法]魔力超载能力获取");
            if (cap.isEnabled()) {
                sequenceNode.setOverloadMultiplier(cap.getCurrentMultiplier());
                Spellweaver.getLOGGER().debug("[SPELLWEAVER:RunesExecuteMethod/spellLogic方法]超载倍数想要设置为{}",cap.getCurrentMultiplier());
                Spellweaver.getLOGGER().debug("[SPELLWEAVER:RunesExecuteMethod/spellLogic方法]超载倍数设置为{}",sequenceNode.getOverloadMultiplier());
            } else {
                sequenceNode.setOverloadMultiplier(1);
                Spellweaver.getLOGGER().debug("[SPELLWEAVER:RunesExecuteMethod/spellLogic方法]超载倍数想要设置为{}",1);
                Spellweaver.getLOGGER().debug("[SPELLWEAVER:RunesExecuteMethod/spellLogic方法]超载倍数设置为{}",sequenceNode.getOverloadMultiplier());
            }
        });
        player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(cap->Spellweaver.getLOGGER().debug("[SPELLWEAVER:RunesExecuteMethod/spellLogic方法]魔力值能力获取"));
        SpellTreeExecuteManager.getInstance().addSpellTree(sequenceNode);
        Spellweaver.getLOGGER().debug("[Spellweaver:RuneExecuteMethod/spellLogic]法术树已推入法术管理器，法术树：{}",sequenceNode.serializeNBT());
    }
    //这个是非玩家专用的执行方法，或者干脆可以说是法术执行方法改进版？
    public static void spellLogic(SequenceNode sequenceNode, Level level, Player player,ManaSource manaSource){
        SpellContext context=new SpellContext(level,player, manaSource);
        context.showErrorMessages = player.getOffhandItem().is(ModItems.MAGICIAN_MIRROR.get()) || player.getMainHandItem().is(ModItems.MAGICIAN_MIRROR.get());
        sequenceNode.setContext(context);
        player.getCapability(PlayerManaOverloadProvider.PLAYER_MANA_OVERLOAD).ifPresent(cap -> {
            Spellweaver.getLOGGER().debug("[SPELLWEAVER:RunesExecuteMethod/新spellLogic方法]魔力超载能力获取");
            if (cap.isEnabled()) {
                sequenceNode.setOverloadMultiplier(cap.getCurrentMultiplier());
                Spellweaver.getLOGGER().debug("[SPELLWEAVER:RunesExecuteMethod/新spellLogic方法]超载倍数想要设置为{}",cap.getCurrentMultiplier());
                Spellweaver.getLOGGER().debug("[SPELLWEAVER:RunesExecuteMethod/新spellLogic方法]超载倍数设置为{}",sequenceNode.getOverloadMultiplier());
            } else {
                sequenceNode.setOverloadMultiplier(1);
                Spellweaver.getLOGGER().debug("[SPELLWEAVER:RunesExecuteMethod/新spellLogic方法]超载倍数想要设置为{}",1);
                Spellweaver.getLOGGER().debug("[SPELLWEAVER:RunesExecuteMethod/新spellLogic方法]超载倍数设置为{}",sequenceNode.getOverloadMultiplier());
            }
        });

        player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(cap->Spellweaver.getLOGGER().debug("[SPELLWEAVER:RunesExecuteMethod/spellLogic方法]魔力值能力获取"));
        SpellTreeExecuteManager.getInstance().addSpellTree(sequenceNode);
        Spellweaver.getLOGGER().debug("[Spellweaver:RuneExecuteMethod/新spellLogic]法术树已推入法术管理器，法术树：{}",sequenceNode.serializeNBT());
    }
    //这个法术执行方法专用于施法机器
    public static void spellLogic(SequenceNode sequenceNode, Level level, Player player, ManaSource manaSource, BlockPos machinePos){
        SpellContext context=new SpellContext(level,player, manaSource,machinePos);
        context.showErrorMessages = player.getOffhandItem().is(ModItems.MAGICIAN_MIRROR.get()) || player.getMainHandItem().is(ModItems.MAGICIAN_MIRROR.get());
        //2026.4.25
        BlockEntity be = context.level.getBlockEntity(machinePos);
        if (be instanceof SpellMachineBlockEntity machine) {
            // 回调逻辑
            context.setOnComplete(state -> {
                //
                if (machine != null && !machine.isRemoved()) {
                    if(state!=NodeResult.RUNNING){
                        Spellweaver.getLOGGER().debug("[Spellweaver:RuneExecuteMethod/机器spellLogic方法]法术结束，执行回调逻辑");
                        machine.setCasting(false);
                        //客户端同步法术状态
                        machine.setChanged();
                        // 手动推送更新到客户端，让纹理消失
                        if (machine.getLevel() != null) {
                            machine.getLevel().sendBlockUpdated(
                                    machinePos,
                                    machine.getBlockState(),
                                    machine.getBlockState(),
                                    Block.UPDATE_ALL
                            );
                        }
                    }
                }
            });
        }
        sequenceNode.setContext(context);
        player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(cap->Spellweaver.getLOGGER().debug("[SPELLWEAVER:RunesExecuteMethod/spellLogic方法]魔力值能力获取"));
        SpellTreeExecuteManager.getInstance().addSpellTree(sequenceNode);
        Spellweaver.getLOGGER().debug("[Spellweaver:RuneExecuteMethod/新spellLogic]法术树已推入法术管理器，法术树：{}",sequenceNode.serializeNBT());
    }
    //这个是给魔法飞弹专用的执行方法，或者说命中回调
    public static void simpleSpellLogic(List<String> spellList, Level level, Player player, Vec3 vec3, @Nullable Entity entity,SpellContext previousContext){
        SpellContext context=new SpellContext(level,player);
        //2026.7.21现在新context会继承老context的魔力源信息，妈妈再也不怕我的魔法飞弹乱消耗魔力了
        ManaSource source=previousContext.manaSource;
        switch (source){
            case MANA_BOTTLE -> {
                context.setManaBottleSlot(previousContext.getManaBottleSlot());
                context.manaSource=ManaSource.MANA_BOTTLE;
            }
            case STICK ->  context.manaSource=ManaSource.STICK;
            case SCROLL -> //卷轴ManaUtil那边直接用的是主手，这不用写啥别的
                    context.manaSource=ManaSource.SCROLL;
            case MACHINE -> {
                context.manaSource=ManaSource.MACHINE;
                context.setMachinePos(previousContext.getMachinePos());
            }
            case PEDESTAL -> {
                context.manaSource=ManaSource.PEDESTAL;
                context.setPedestalPos(previousContext.getPedestalPos());
            }
        }
        context.showErrorMessages = player.getOffhandItem().is(ModItems.MAGICIAN_MIRROR.get()) || player.getMainHandItem().is(ModItems.MAGICIAN_MIRROR.get());
        context.push(vec3);
        if(entity!=null){
            context.entity=entity;
        }
        if (context != null) {
            int i = 0;
            int maxIterations = 100; // 每帧最大循环的安全限制，防止主线程卡死,2026.3.16新增
            int iterations = 0;
            while (i < spellList.size()) {
                String rune = spellList.get(i);
                if ("[".equals(rune)) {
                    List<String> collected = new ArrayList<>();
                    i++;
                    while (i < spellList.size()) {
                        String next = spellList.get(i);
                        if ("]".equals(next)) {
                            break;
                        }
                        collected.add(next);
                        i++;
                    }
                    i++;
                    RuneRegister register = new RuneRegister(collected);
                    context.push(register);
                    continue;
                }else if ("跳转".equals(rune)&&context.jumpTarget>=0&& context.jumpTarget <spellList.size()) {
                    if(iterations<maxIterations){
                        i= context.jumpTarget;
                        iterations++;
                    }else {
                        i++;
                        iterations=0;
                        context.jumpTarget=-1;
                    }
                    continue;
                }
                SpellExecutor executor = SpellExecutorManager.getInstance().getExecutor(rune);
                if (executor != null) {
                    try {
                        executor.execute(context);
                    } catch (SpellExecutionException e) {
                        if (context.showErrorMessages) {
                            context.player.sendSystemMessage(
                                    Component.translatable("message.spellweaver.rune_param_error", rune, e.getMessage())
                            );
                        }
                        break;
                    }
                } else {
                    if (rune.startsWith("\"") && rune.endsWith("\"")) {
                        String string = rune.substring(1, rune.length() - 1);
                        context.push(string);
                    } else {
                        try {
                            double number = Double.parseDouble(rune);
                            context.push(number);
                        } catch (NumberFormatException ex) {
                            context.player.sendSystemMessage(
                                    Component.translatable("message.spellweaver.unknown_rune", rune)
                            );
                        }
                    }
                }
                i++;
            }
        }
    }
    //幻化武器专用方法
    public static void ManaSwordSpellLogic(List<String> spellList, Level level, Player player, LivingEntity targetEntity){
        SpellContext context=new SpellContext(level,player);
        context.showErrorMessages = player.getOffhandItem().is(ModItems.MAGICIAN_MIRROR.get()) || player.getMainHandItem().is(ModItems.MAGICIAN_MIRROR.get());
        context.push(targetEntity);
        if (context != null) {
            int i = 0;
            int maxIterations = 100; // 每帧最大循环的安全限制，防止主线程卡死,2026.3.16新增
            int iterations = 0;
            while (i < spellList.size()) {
                String rune = spellList.get(i);
                if ("[".equals(rune)) {
                    List<String> collected = new ArrayList<>();
                    i++;
                    while (i < spellList.size()) {
                        String next = spellList.get(i);
                        if ("]".equals(next)) {
                            break;
                        }
                        collected.add(next);
                        i++;
                    }
                    i++;
                    RuneRegister register = new RuneRegister(collected);
                    context.push(register);
                    continue;
                }else if ("跳转".equals(rune)&&context.jumpTarget>=0&& context.jumpTarget <spellList.size()) {
                    if(iterations<maxIterations){
                        i= context.jumpTarget;
                        iterations++;
                    }else {
                        i++;
                        iterations=0;
                        context.jumpTarget=-1;
                    }
                    continue;
                }
                SpellExecutor executor = SpellExecutorManager.getInstance().getExecutor(rune);
                if (executor != null) {
                    try {
                        executor.execute(context);
                    } catch (SpellExecutionException e) {
                        if (context.showErrorMessages) {
                            context.player.sendSystemMessage(
                                    Component.translatable("message.spellweaver.rune_param_error", rune, e.getMessage())
                            );
                        }
                        break;
                    }
                } else {
                    if (rune.startsWith("\"") && rune.endsWith("\"")) {
                        String string = rune.substring(1, rune.length() - 1);
                        context.push(string);
                    } else {
                        try {
                            double number = Double.parseDouble(rune);
                            context.push(number);
                        } catch (NumberFormatException ex) {
                            context.player.sendSystemMessage(
                                    Component.translatable("message.spellweaver.unknown_rune", rune)
                            );
                        }
                    }
                }
                i++;
            }
        }
    }
    //这个是被羽毛笔写入的方块、物品用的
    public static void triggerSpell(ServerPlayer player, Level level, CompoundTag spellDataTag,
                                    Consumer<SpellContext> contextCustomizer) {
        if (spellDataTag == null || spellDataTag.isEmpty()) return;

        SequenceNode node = new SequenceNode();
        node.deserializeNBT(spellDataTag);
        SpellContext context = new SpellContext (level,player);
        context.showErrorMessages = player.getOffhandItem().is(ModItems.MAGICIAN_MIRROR.get()) || player.getMainHandItem().is(ModItems.MAGICIAN_MIRROR.get());


        if (contextCustomizer != null) {
            contextCustomizer.accept(context);
        }

        node.setContext(context);
        SpellTreeExecuteManager.getInstance().addSpellTree(node);
        Spellweaver.getLOGGER().debug("[Spellweaver/ModEvent/triggerSpell]法术已推入");

    }
}
