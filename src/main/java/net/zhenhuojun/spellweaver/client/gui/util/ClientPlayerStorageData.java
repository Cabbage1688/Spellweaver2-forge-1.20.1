package net.zhenhuojun.spellweaver.client.gui.util;

import net.zhenhuojun.spellweaver.capability.impl.spell_storage.PlayerSpellStorage;

public class ClientPlayerStorageData {
    private static PlayerSpellStorage playerSpellStorage;

    public static PlayerSpellStorage getPlayerSpellStorage() {

        return playerSpellStorage;
    }

    public static void setPlayerSpellStorage(PlayerSpellStorage playerSpellStorage) {
        ClientPlayerStorageData.playerSpellStorage = playerSpellStorage;
    }

}
