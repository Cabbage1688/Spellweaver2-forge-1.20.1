package net.zhenhuojun.spellweaver.client.gui.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientManaShieldData {
    private static boolean shieldActive;
    private static double shieldAmount;

    public static void setActive(boolean active) {
        ClientManaShieldData.shieldActive = active;
    }

    public static void setShieldAmount(double amount) {
        ClientManaShieldData.shieldAmount = amount;
    }

    public static boolean isShieldActive() {
        return shieldActive;
    }

    public static double getShieldAmount() {
        return shieldAmount;
    }
}