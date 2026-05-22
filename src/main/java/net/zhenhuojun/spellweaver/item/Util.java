package net.zhenhuojun.spellweaver.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.zhenhuojun.spellweaver.spell.RuneRegister;

import java.util.List;


public class Util {
    //召唤并设置法术
    public static ItemStack summonManaSword(RuneRegister runeRegister){
        ItemStack swordStack=new ItemStack(ModItems.MANA_SWORD.get());
        if(!runeRegister.getSpellList().isEmpty()){
            List<String> spellList=runeRegister.getSpellList().stream().toList();
            CompoundTag tag=swordStack.getOrCreateTag();
            ListTag spellListTag = new ListTag();
            for(String rune:spellList){
                spellListTag.add(StringTag.valueOf(rune));
            }
            tag.put("spellList",spellListTag);
        }
        return swordStack;
    }
    public static ItemStack summonManaBow(RuneRegister runeRegister){
        ItemStack bowStack=new ItemStack(ModItems.MANA_BOW.get());
        if(!runeRegister.getSpellList().isEmpty()){
            List<String> spellList=runeRegister.getSpellList().stream().toList();
            CompoundTag tag=bowStack.getOrCreateTag();
            ListTag spellListTag = new ListTag();
            for(String rune:spellList){
                spellListTag.add(StringTag.valueOf(rune));
            }
            tag.put("spellList",spellListTag);
        }
        return bowStack;
    }
}
