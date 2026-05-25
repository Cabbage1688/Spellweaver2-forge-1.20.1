package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.StoredSpell;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerSpellStorageProvider;
import net.zhenhuojun.spellweaver.network.ModMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
//当玩家在署名界面署名后，使用这个包更新法术的作者列表
public class UpdateSpellAuthorsC2SPacket {
    private final UUID spellId;
    private final List<String> authors;

    public UpdateSpellAuthorsC2SPacket(UUID spellId, List<String> authors) {
        this.spellId = spellId;
        this.authors = authors;
    }

    public UpdateSpellAuthorsC2SPacket(FriendlyByteBuf buf) {
        this.spellId = buf.readUUID();
        int size = buf.readInt();
        this.authors = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.authors.add(buf.readUtf(64));
        }
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeUUID(spellId);
        buf.writeInt(authors.size());
        for (String author : authors) {
            buf.writeUtf(author, 64);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(PlayerSpellStorageProvider.PLAYER_SPELL_STORAGE).ifPresent(storage -> {
                    StoredSpell spell = storage.getSpells().get(spellId);
                    if (spell != null) {
                        spell.getAuthors().clear();
                        spell.getAuthors().addAll(authors);
                        // 同步整个存储给客户端
                        ModMessage.sendToPlayer(new SpellStorageSyncS2CPacket(storage.serialize()), player);
                    }
                });
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
