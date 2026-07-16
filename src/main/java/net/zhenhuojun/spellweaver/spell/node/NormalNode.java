package net.zhenhuojun.spellweaver.spell.node;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.spell.*;

import java.util.ArrayList;
import java.util.List;

public class NormalNode implements Node{
    private List<String> spellList=new ArrayList<>();
    private SpellContext context;

    @Override
    public NodeEnum getEnum() {
        return NodeEnum.NORMAL;
    }

    public String getType() {
        return "normal";
    }


    public NodeResult executeSpell(SpellContext context) {
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
                } else if ("跳转".equals(rune)&&context.jumpTarget>=0&& context.jumpTarget <spellList.size()) {
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
                    // 尝试解析为字符串常量（带引号）
                    if (rune.startsWith("\"") && rune.endsWith("\"")) {
                        String string = rune.substring(1, rune.length() - 1);
                        context.push(string);
                    }
                    // 尝试解析为数字常量
                    else {
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
            this.context = context;
            return NodeResult.SUCCESS;
        }
        return NodeResult.FAULT;
    }

    public SpellContext getContext() {
        return context;
    }

    public List<String> getSpellList(){
        return spellList;
    }
    public void setSpellList(List<String> spellList){
        this.spellList=spellList;
    }
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", getType());

        ListTag spellListTag = new ListTag();
        for(String rune:spellList){
            spellListTag.add(StringTag.valueOf(rune));
        }
        tag.put("spellList",spellListTag);
        return tag;
    }
    @Override
    public void deserializeNBT(CompoundTag tag) {
        spellList.clear();
        ListTag spellListTag=tag.getList("spellList", Tag.TAG_STRING);
        for(int i=0;i<spellListTag.size();i++){
            spellList.add(spellListTag.getString(i));
        }
    }
}
