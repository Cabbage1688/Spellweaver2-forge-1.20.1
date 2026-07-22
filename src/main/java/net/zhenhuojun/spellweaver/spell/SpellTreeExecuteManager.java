package net.zhenhuojun.spellweaver.spell;

import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.spell.node.NodeResult;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class SpellTreeExecuteManager {
    private static SpellTreeExecuteManager instance;
    private final Map<UUID, SequenceNode> activeSpellTree=new HashMap<>();//该表储存活跃（即正在运行的法术树）

    public static SpellTreeExecuteManager getInstance() {
        if(instance==null){
            instance=new SpellTreeExecuteManager();
        }
        return instance;
    }
    //把法术树推入Map
    public void addSpellTree(SequenceNode sequenceNode){
        activeSpellTree.put(sequenceNode.getUuid(), sequenceNode);
    }
    //TODO下次更新出一个取消正在运行的法术的功能
    public void cancelSpellTree(UUID uuid){
        SequenceNode sequenceNode=activeSpellTree.get(uuid);
        if(sequenceNode!=null){
            activeSpellTree.remove(uuid);
        }
    }
    public void cancelAllSpellsForPlayer(Player player) {
        Iterator<Map.Entry<UUID, SequenceNode>> iterator = activeSpellTree.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, SequenceNode> entry = iterator.next();
            SequenceNode sequenceNode = entry.getValue();
            if (sequenceNode.getContext() != null && sequenceNode.getContext().player.getUUID().equals(player.getUUID())) {
                sequenceNode.getContext().notifyComplete(NodeResult.FAULT);
                iterator.remove();
                Spellweaver.getLOGGER().debug("[SPELLWEAVER:SpellExecuteManager/cancelAllSpellsForPlayer]取消玩家{}的法术树{}", player.getName().getString(), sequenceNode.getUuid());
            }
        }
    }
    //这个丢给ServerTickEvent
    public void tick(){
        //获取迭代器
        Iterator<Map.Entry<UUID,SequenceNode>> iterator=activeSpellTree.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID,SequenceNode> entry=iterator.next();
            SequenceNode sequenceNode=entry.getValue();
            Spellweaver.getLOGGER().debug("[SPELLWEAVER:SpellExecuteManager/tick方法]超载倍数{}",sequenceNode.getOverloadMultiplier());
            if(sequenceNode.getOverloadMultiplier()<=1){
                switch (sequenceNode.getState()){
                    case RUNNING -> sequenceNode.tick();
                    case FAULT -> {
                        iterator.remove();
                        sequenceNode.getContext().notifyComplete(NodeResult.FAULT);
                        Spellweaver.getLOGGER().debug("[SPELLWEAVER:SpellExecuteManager/tick方法]法术执行异常");
                    }
                    case SUCCESS -> {
                        iterator.remove();
                        sequenceNode.getContext().notifyComplete(NodeResult.SUCCESS);
                        Spellweaver.getLOGGER().debug("[SPELLWEAVER:SpellExecuteManager/tick方法]法术运行完毕");
                    }
                }
            }else {
               outerFor:
                for(int i=0;i<sequenceNode.getOverloadMultiplier();i++){
                    switch (sequenceNode.getState()){
                        case RUNNING -> sequenceNode.tick();
                        case FAULT -> {
                            iterator.remove();
                            sequenceNode.getContext().notifyComplete(NodeResult.FAULT);
                            Spellweaver.getLOGGER().debug("[SPELLWEAVER:SpellExecuteManager/tick方法]法术执行异常");
                            break outerFor;   // 终止整个 for 循环
                        }
                        case SUCCESS -> {
                            iterator.remove();
                            sequenceNode.getContext().notifyComplete(NodeResult.SUCCESS);
                            Spellweaver.getLOGGER().debug("[SPELLWEAVER:SpellExecuteManager/tick方法]法术运行完毕");
                            break outerFor;   // 终止整个 for 循环
                        }
                    }
                }
            }


        }
    }


    /*public void tick() {
        Iterator<Map.Entry<UUID, SequenceNode>> iterator = activeSpellTree.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, SequenceNode> entry = iterator.next();
            SequenceNode sequenceNode = entry.getValue();

            if (sequenceNode.getState() != NodeResult.RUNNING) {
                // 理论上不会进来，但以防万一
                iterator.remove();
                continue;
            }

            int maxSteps = sequenceNode.getOverloadMultiplier();
            int stepsDone = 0;
            boolean continueThisTick = true;

            while (stepsDone < maxSteps && continueThisTick) {
                sequenceNode.tick();   // 执行一帧逻辑，内部会更新 state

                switch (sequenceNode.getState()) {
                    case RUNNING:
                        stepsDone++;
                        // 若 stepsDone 达到上限，循环结束，树保持 RUNNING 等待下一 tick
                        break;
                    case SUCCESS:
                    case FAULT:
                        // 树已完成或异常，立即结束循环并在外层移除
                        continueThisTick = false;
                        break;
                }
            }

            // 循环结束后检查最终状态
            NodeResult finalState = sequenceNode.getState();
            if (finalState == NodeResult.SUCCESS) {
                iterator.remove();
                Spellweaver.getLOGGER().debug("法术树 {} 执行成功", sequenceNode.getUuid());
            } else if (finalState == NodeResult.FAULT) {
                iterator.remove();
                Spellweaver.getLOGGER().debug("法术树 {} 执行失败", sequenceNode.getUuid());
            }
            // 如果是 RUNNING，保留在 Map 中，下一 tick 继续
        }
    }

     */
    /**
     * Iterator是迭代器，它只能向前遍历而不能双向遍历，此外它应该是唯一能够在遍历中安全删除元素的了。
     * Map.Entry 是 Map 中的一个键值对条目，代表 Map 中的一组映射关系。它是 Map 内部使用的数据结构
     */

}
