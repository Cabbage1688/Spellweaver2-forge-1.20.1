package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.SpellListEntry;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.StoredSpell;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerSpellStorageProvider;
import net.zhenhuojun.spellweaver.entity.impl.MagicStarEntity;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.item.custom.MagicStarItem;
import net.zhenhuojun.spellweaver.network.ModMessage;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 客户端→服务器：对魔法之星实体执行操作。
 * 操作类型：
 *  0 = CHANGE_MODE（参数：modeOrdinal）
 *  1 = ADD_SPELL  （参数：listType, spellUUID —— 从主人法术库复制到实体列表）
 *  2 = REMOVE_SPELL（参数：listType, spellUUID）
 *  3 = MOVE_UP    （参数：listType, spellUUID）
 *  4 = MOVE_DOWN  （参数：listType, spellUUID）
 *  5 = TOGGLE_DISABLE（参数：listType, spellUUID）
 *  6 = ADD_DELAY  （参数：listType, modeOrdinal=delayTicks）
 *  7 = ITEMIZE    （参数：无）将实体转为ItemStack并在原位置掉落ItemEntity
 */
public class MagicStarActionC2SPacket {
    private final int entityId;
    private final int action;
    private final int modeOrdinal;   // CHANGE_MODE时使用
    private final int listType;      // 0=attack,1=shield,2=protectMaster,3=routine
    private final UUID spellUUID;    // ADD/REMOVE/MOVE时使用

    public MagicStarActionC2SPacket(int entityId, int action, int modeOrdinal, int listType, UUID spellUUID) {
        this.entityId = entityId;
        this.action = action;
        this.modeOrdinal = modeOrdinal;
        this.listType = listType;
        this.spellUUID = spellUUID;
    }

    /** 切换模式 */
    public static MagicStarActionC2SPacket changeMode(int entityId, int modeOrdinal) {
        return new MagicStarActionC2SPacket(entityId, 0, modeOrdinal, 0, null);
    }
    /** 添加法术到列表（从主人法术库复制） */
    public static MagicStarActionC2SPacket addSpell(int entityId, int listType, UUID spellUUID) {
        return new MagicStarActionC2SPacket(entityId, 1, 0, listType, spellUUID);
    }
    /** 删除法术 */
    public static MagicStarActionC2SPacket removeSpell(int entityId, int listType, UUID spellUUID) {
        return new MagicStarActionC2SPacket(entityId, 2, 0, listType, spellUUID);
    }
    /** 上移 */
    public static MagicStarActionC2SPacket moveUp(int entityId, int listType, UUID spellUUID) {
        return new MagicStarActionC2SPacket(entityId, 3, 0, listType, spellUUID);
    }
    /** 下移 */
    public static MagicStarActionC2SPacket moveDown(int entityId, int listType, UUID spellUUID) {
        return new MagicStarActionC2SPacket(entityId, 4, 0, listType, spellUUID);
    }
    /** 切换法术禁用状态 */
    public static MagicStarActionC2SPacket toggleDisable(int entityId, int listType, UUID spellUUID) {
        return new MagicStarActionC2SPacket(entityId, 5, 0, listType, spellUUID);
    }
    /** 添加延迟条目 */
    public static MagicStarActionC2SPacket addDelay(int entityId, int listType, int delayTicks) {
        return new MagicStarActionC2SPacket(entityId, 6, delayTicks, listType, null);
    }
    /** 将实体转为物品掉落 */
    public static MagicStarActionC2SPacket itemize(int entityId) {
        return new MagicStarActionC2SPacket(entityId, 7, 0, 0, null);
    }

    public MagicStarActionC2SPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.action = buf.readVarInt();
        this.modeOrdinal = buf.readVarInt();
        this.listType = buf.readVarInt();
        this.spellUUID = buf.readBoolean() ? buf.readUUID() : null;
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(action);
        buf.writeVarInt(modeOrdinal);
        buf.writeVarInt(listType);
        if (spellUUID != null) {
            buf.writeBoolean(true);
            buf.writeUUID(spellUUID);
        } else {
            buf.writeBoolean(false);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (!(player.level() instanceof ServerLevel serverLevel)) return;
            Entity entity = serverLevel.getEntity(entityId);
            if (!(entity instanceof MagicStarEntity magicStar)) return;
            // 仅主人可操作
            if (magicStar.getOwnerUUID() == null || !magicStar.getOwnerUUID().equals(player.getUUID())) return;

            switch (action) {
                case 0 -> handleChangeMode(magicStar);
                case 1 -> handleAddSpell(magicStar, player);
                case 2 -> handleRemoveSpell(magicStar);
                case 3 -> handleMove(magicStar, -1);
                case 4 -> handleMove(magicStar, +1);
                case 5 -> handleToggleDisable(magicStar);
                case 6 -> handleAddDelay(magicStar);
                case 7 -> handleItemize(magicStar, serverLevel);
            }
            // ITEMIZE操作后实体已被移除，不需要再syncToOwner
            if (action != 7) {
                magicStar.syncToOwner();
            }
        });
        return true;
    }

    private void handleChangeMode(MagicStarEntity e) {
        MagicStarEntity.ActMode[] modes = MagicStarEntity.ActMode.values();
        if (modeOrdinal < 0 || modeOrdinal >= modes.length) return;
        e.setActMode(modes[modeOrdinal]);
    }

    private void handleAddSpell(MagicStarEntity e, ServerPlayer player) {
        if (spellUUID == null) return;
        // 从主人法术库查源法术
        var storageOpt = player.getCapability(PlayerSpellStorageProvider.PLAYER_SPELL_STORAGE);
        if (!storageOpt.isPresent()) return;
        var storage = storageOpt.orElse(null);
        var source = storage.getSpell(spellUUID);
        if (source.isEmpty()) return;
        // 复制一份StoredSpell（深拷贝：序列化再反序列化，避免引用同一SequenceNode）
        StoredSpell copy = StoredSpell.deserialize(source.get().serialize());
        List<SpellListEntry> list = getList(e);
        // 防重复
        for (SpellListEntry entry : list) {
            if (entry.isSpell() && entry.getSpell().getId().equals(copy.getId())) return;
        }
        list.add(SpellListEntry.ofSpell(copy));
    }

    private void handleAddDelay(MagicStarEntity e) {
        int delayTicks = modeOrdinal;
        if (delayTicks <= 0) return;
        getList(e).add(SpellListEntry.ofDelay(delayTicks));
    }

    private void handleRemoveSpell(MagicStarEntity e) {
        if (spellUUID == null) return;
        getList(e).removeIf(entry -> entry.getId().equals(spellUUID));
    }

    private void handleMove(MagicStarEntity e, int delta) {
        if (spellUUID == null) return;
        List<SpellListEntry> list = getList(e);
        int idx = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(spellUUID)) { idx = i; break; }
        }
        if (idx < 0) return;
        int newIdx = idx + delta;
        if (newIdx < 0 || newIdx >= list.size()) return;
        SpellListEntry tmp = list.get(idx);
        list.set(idx, list.get(newIdx));
        list.set(newIdx, tmp);
    }

    private List<SpellListEntry> getList(MagicStarEntity e) {
        return switch (listType) {
            case 0 -> e.getAttackSpells();
            case 1 -> e.getShieldSpells();
            case 2 -> e.getProtectMasterSpells();
            case 3 -> e.getRoutineSpells();
            default -> throw new IllegalArgumentException("Unknown list type: " + listType);
        };
    }

    private void handleToggleDisable(MagicStarEntity e) {
        if (spellUUID == null) return;
        e.toggleSpellDisabled(listType, spellUUID);
    }

    private void handleItemize(MagicStarEntity e, ServerLevel serverLevel) {
        // 保存实体的所有NBT数据（包含位置/朝向/法术/模式等）
        CompoundTag entityData = new CompoundTag();
        e.saveWithoutId(entityData);
        // 构建物品栈并将实体NBT保存在EntityData标签下
        ItemStack stack = new ItemStack(ModItems.MAGIC_STAR_ITEM.get());
        stack.addTagElement(MagicStarItem.ENTITY_DATA_TAG, entityData);
        // 在实体原位置生成掉落物
        ItemEntity itemEntity = new ItemEntity(
                serverLevel,
                e.getX(), e.getY(), e.getZ(),
                stack
        );
        serverLevel.addFreshEntity(itemEntity);
        // 移除原实体
        e.discard();
    }
}
