package net.zhenhuojun.spellweaver.client.gui.util;

import net.zhenhuojun.spellweaver.capability.impl.long_term_variables.PlayerLongTermVariablesData;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerLongTermVariablesProvider;

public class ClientPlayerVariableData {
    private static PlayerLongTermVariablesData playerLongTermVariablesData;

    public static PlayerLongTermVariablesData getPlayerLongTermVariablesData() {
        return playerLongTermVariablesData;
    }

    public static void setPlayerLongTermVariablesData(PlayerLongTermVariablesData playerLongTermVariablesData) {
        ClientPlayerVariableData.playerLongTermVariablesData = playerLongTermVariablesData;
    }
}
