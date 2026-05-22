package net.zhenhuojun.spellweaver.client.data_util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientPlayerOverloadData {
    private static boolean enabled = false;
    private static int currentMultiplier = 1;
    private static int maxMultiplier = 1;

    // 启用状态
    public static void setEnabled(boolean enabled) {
        ClientPlayerOverloadData.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    // 当前倍率
    public static void setCurrentMultiplier(int currentMultiplier) {
        ClientPlayerOverloadData.currentMultiplier = currentMultiplier;
    }

    public static int getCurrentMultiplier() {
        return currentMultiplier;
    }

    // 最大倍率
    public static void setMaxMultiplier(int maxMultiplier) {
        ClientPlayerOverloadData.maxMultiplier = maxMultiplier;
    }

    public static int getMaxMultiplier() {
        return maxMultiplier;
    }

    public static void addCurrentMultiplier(int add){
        ClientPlayerOverloadData.currentMultiplier = Math.min(currentMultiplier + add, maxMultiplier);
    }

    public static void subCurrentMultiplier(int sub){
        ClientPlayerOverloadData.currentMultiplier = Math.max(currentMultiplier -sub, 1);
    }
}
