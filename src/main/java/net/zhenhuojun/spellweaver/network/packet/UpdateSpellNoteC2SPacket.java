package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.StoredSpell;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerSpellStorageProvider;
import net.zhenhuojun.spellweaver.network.ModMessage;

import java.util.UUID;
import java.util.function.Supplier;

public class UpdateSpellNoteC2SPacket {
    private final UUID spellId;
    private final String note;

    public UpdateSpellNoteC2SPacket(UUID spellId, String note) {
        this.spellId = spellId;
        this.note = note;
    }

    public UpdateSpellNoteC2SPacket(FriendlyByteBuf buf) {
        this.spellId = buf.readUUID();
        this.note = buf.readUtf(256); // 适当长度限制
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeUUID(spellId);
        buf.writeUtf(note, 256);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(PlayerSpellStorageProvider.PLAYER_SPELL_STORAGE).ifPresent(storage -> {
                    StoredSpell spell = storage.getSpells().get(spellId);
                    if (spell != null) {
                        spell.setNote(note);
                        // 同步整个存储到客户端
                        ModMessage.sendToPlayer(new SpellStorageSyncS2CPacket(storage.serialize()), player);
                    }
                });
            }
        });
        //标记数据包成功处理
        context.setPacketHandled(true);
        return true;
    }
}
