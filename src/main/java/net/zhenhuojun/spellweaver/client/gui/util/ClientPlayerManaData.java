package net.zhenhuojun.spellweaver.client.gui.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientPlayerManaData {
    private static double playerMana;
    private static long maxMana =100;
    private static int manaLevel;
    private static long manaExp;
    private static long currentExp;

    //设置魔力值
    public static void set(double mana){
        ClientPlayerManaData.playerMana=mana;
    }
    public static void setMaxMana(long maxMana) {
        ClientPlayerManaData.maxMana = maxMana;
    }
    public static void setManaLevel(int manaLevel) {
        ClientPlayerManaData.manaLevel = manaLevel;
    }

    //获取魔力值数值
    public static double getPlayerMana(){
        return  playerMana;
    }

    public static long getMaxMana(){
        return maxMana;
    }
    public static  int getManaLevel(){
        return manaLevel;
    }
    public static void setManaExp(long manaExp){
        ClientPlayerManaData.manaExp =manaExp;
    }
    public static long getManaExp(){
        return manaExp;
    }

    public static void setCurrentExp(long currentExp){
        ClientPlayerManaData.currentExp=currentExp;
    }

    public static long getCurrentExp() {
        return currentExp;
    }
}