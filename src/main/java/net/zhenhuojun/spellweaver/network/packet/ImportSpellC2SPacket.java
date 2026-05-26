package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.PlayerSpellStorage;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.StoredSpell;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerSpellStorageProvider;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ImportSpellC2SPacket {
    private final String spellName;
    private final CompoundTag spellTag;
    private final List<String> authors;
    private final String note;
    private final UUID existingId; // null 表示新法术，非空表示覆盖

    public ImportSpellC2SPacket(String spellName, CompoundTag spellTag, List<String> authors,String note,  UUID existingId) {
        this.spellName = spellName;
        this.spellTag = spellTag;
        this.authors = authors;
        this.note = (note != null) ? note : "";
        this.existingId = existingId;
    }

    public ImportSpellC2SPacket(FriendlyByteBuf buf) {
        this.spellName = buf.readUtf();
        this.spellTag = buf.readNbt();
        int size = buf.readInt();
        this.authors = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.authors.add(buf.readUtf(64));
        }
        this.note = buf.readUtf(256);
        if (buf.readBoolean()) {
            this.existingId = buf.readUUID();
        } else {
            this.existingId = null;
        }

    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeUtf(spellName);
        buf.writeNbt(spellTag);
        buf.writeInt(authors.size());
        for (String author : authors) {
            buf.writeUtf(author, 64);
        }
        buf.writeUtf(note, 256);
        buf.writeBoolean(existingId != null);
        if (existingId != null) {
            buf.writeUUID(existingId);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(PlayerSpellStorageProvider.PLAYER_SPELL_STORAGE).ifPresent(storage -> {
                    SequenceNode sequenceNode = new SequenceNode();
                    sequenceNode.deserializeNBT(spellTag);

                    if (existingId != null) {
                        // 覆盖已有法术
                        StoredSpell existing = storage.getSpells().get(existingId);
                        if (existing != null) {
                            existing.rename(spellName);
                            existing.setSequenceNode(sequenceNode);
                            existing.getAuthors().clear();
                            existing.getAuthors().addAll(authors);
                            existing.setNote(note);
                        }
                    } else {
                        // 新增法术
                        if (storage.getSpells().size() >= PlayerSpellStorage.MAX_STORED_SPELLS) return;
                        StoredSpell newSpell = new StoredSpell(UUID.randomUUID(), spellName, sequenceNode, authors,note);
                        storage.getSpells().put(newSpell.getId(), newSpell);
                    }

                    // 同步整个存储到客户端
                    ModMessage.sendToPlayer(new SpellStorageSyncS2CPacket(storage.serialize()), player);
                });
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
