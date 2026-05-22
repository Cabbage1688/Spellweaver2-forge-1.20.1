package net.zhenhuojun.spellweaver.client.gui.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientPlayerManaData {
    private static double playerMana;
    private static int MaxMana=100;
    private static int ManaLevel;

    //设置魔力值
    public static void set(double mana){
        ClientPlayerManaData.playerMana=mana;
    }
    public static void setMaxMana(int maxMana) {
        MaxMana = maxMana;
    }
    public static void setManaLevel(int manaLevel) {
        ManaLevel = manaLevel;
    }

    //获取魔力值数值
    public static double getPlayerMana(){
        return  playerMana;
    }

    public static int getMaxMana(){
        return MaxMana;
    }
    public static  int getManaLevel(){
        return ManaLevel;
    }
}