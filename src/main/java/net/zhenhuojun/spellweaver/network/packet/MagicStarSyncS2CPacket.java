package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.SpellListEntry;
import net.zhenhuojun.spellweaver.client.gui.MagicStarScreen;
import net.zhenhuojun.spellweaver.client.gui.util.ClientMagicStarData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 服务器→客户端：同步魔法之星实体数据。
 * openGui=true时客户端打开MagicStarScreen。
 */
public class MagicStarSyncS2CPacket {
    private final int entityId;
    private final int actModeOrdinal;
    private final CompoundTag spellsTag; // 含四个ListTag
    private final boolean openGui;

    public MagicStarSyncS2CPacket(int entityId, int actModeOrdinal, CompoundTag spellsTag, boolean openGui) {
        this.entityId = entityId;
        this.actModeOrdinal = actModeOrdinal;
        this.spellsTag = spellsTag;
        this.openGui = openGui;
    }

    public MagicStarSyncS2CPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.actModeOrdinal = buf.readVarInt();
        this.spellsTag = buf.readNbt();
        this.openGui = buf.readBoolean();
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(actModeOrdinal);
        buf.writeNbt(spellsTag);
        buf.writeBoolean(openGui);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            List<SpellListEntry> attack = deserializeList(spellsTag.getList("AttackSpells", Tag.TAG_COMPOUND));
            List<SpellListEntry> shield = deserializeList(spellsTag.getList("ShieldSpells", Tag.TAG_COMPOUND));
            List<SpellListEntry> protectMaster = deserializeList(spellsTag.getList("ProtectMasterSpells", Tag.TAG_COMPOUND));
            List<SpellListEntry> routine = deserializeList(spellsTag.getList("RoutineSpells", Tag.TAG_COMPOUND));
            // 解析禁用法术集合（按列表独立）
            Map<Integer, Set<UUID>> disabled = new HashMap<>();
            CompoundTag disabledTag = spellsTag.getCompound("DisabledSpells");
            for (String key : disabledTag.getAllKeys()) {
                int listType = Integer.parseInt(key);
                ListTag listTag = disabledTag.getList(key, Tag.TAG_INT_ARRAY);
                Set<UUID> set = new HashSet<>();
                for (int i = 0; i < listTag.size(); i++) {
                    set.add(NbtUtils.loadUUID(listTag.get(i)));
                }
                disabled.put(listType, set);
            }
            ClientMagicStarData.updateAll(entityId, actModeOrdinal, attack, shield, protectMaster, routine, disabled);

            if (openGui && Minecraft.getInstance().screen == null) {
                Minecraft.getInstance().setScreen(new MagicStarScreen());
            }
        });
        return true;
    }

    private static List<SpellListEntry> deserializeList(ListTag tag) {
        List<SpellListEntry> list = new ArrayList<>();
        for (int i = 0; i < tag.size(); i++) {
            list.add(SpellListEntry.deserialize(tag.getCompound(i)));
        }
        return list;
    }
}
