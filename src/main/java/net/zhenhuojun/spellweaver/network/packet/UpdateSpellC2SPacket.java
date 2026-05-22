package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.StoredSpell;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerSpellStorageProvider;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;

import java.util.UUID;
import java.util.function.Supplier;

public class UpdateSpellC2SPacket {
    private final UUID spellId;
    private final CompoundTag spellTag;    // 更新后的节点树数据

    public UpdateSpellC2SPacket(UUID spellId, CompoundTag spellTag) {
        this.spellId = spellId;
        this.spellTag = spellTag;
    }

    public UpdateSpellC2SPacket(FriendlyByteBuf buf) {
        this.spellId = buf.readUUID();
        this.spellTag = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(spellId);
        buf.writeNbt(spellTag);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(PlayerSpellStorageProvider.PLAYER_SPELL_STORAGE).ifPresent(playerSpellStorage -> {
                    // 从能力中查找对应ID的法术
                    StoredSpell existingSpell = playerSpellStorage.getSpell(spellId).orElse(null);
                    if (existingSpell != null) {
                        // 反序列化节点树
                        SequenceNode updatedNode = new SequenceNode();
                        updatedNode.deserializeNBT(spellTag);
                        // 更新法术的节点树
                        existingSpell.setSequenceNode(updatedNode);
                        // 标记能力数据已改变（如果需要自动保存）
                        // playerSpellStorage.serialize();  // 若能力自动同步则无需手动
                        // 可选：将更新后的完整存储同步回客户端，确保一致性
                        ModMessage.sendToPlayer(new SpellStorageSyncS2CPacket(playerSpellStorage.serialize()), player);
                    }
                });
            }
        });
        return true;
    }
}
