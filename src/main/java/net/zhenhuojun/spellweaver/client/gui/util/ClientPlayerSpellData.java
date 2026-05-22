package net.zhenhuojun.spellweaver.client.gui.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
//这个辅助类好像是用于法术复制和缓存来着
public class ClientPlayerSpellData {
    private static final Map<UUID, ClientPlayerSpellData> PLAYER_DATA = new HashMap<>();

    private CompoundTag spellTag;
    private CompoundTag copyTag;

    public static ClientPlayerSpellData get(Player player) {
        return PLAYER_DATA.computeIfAbsent(player.getUUID(), k -> new ClientPlayerSpellData());
    }

    public void setCopyTag(CompoundTag copyTag) {
        this.copyTag = copyTag;
    }

    public void setSpellTag(CompoundTag spellTag) {
        this.spellTag = spellTag;
    }

    public CompoundTag getCopyTag() {
        return copyTag;
    }

    public CompoundTag getSpellTag() {
        return spellTag;
    }
}
