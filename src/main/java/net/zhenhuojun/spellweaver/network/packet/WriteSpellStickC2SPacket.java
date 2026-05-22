package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.item.custom.SpellStickItem;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;

import java.util.function.Supplier;

public class WriteSpellStickC2SPacket {
    private final String spellName;
    private final CompoundTag spellTag; // 这里存放的是 sequence 标签

    public WriteSpellStickC2SPacket(String spellName, CompoundTag spellTag) {
        this.spellName = spellName;
        this.spellTag = spellTag;
    }

    public WriteSpellStickC2SPacket(FriendlyByteBuf buf) {
        this.spellName = buf.readUtf();
        this.spellTag = buf.readNbt();
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeUtf(spellName);
        buf.writeNbt(spellTag);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            ItemStack wandStack = player.getMainHandItem();
            if (wandStack.isEmpty() || !(wandStack.getItem() instanceof SpellStickItem)) {
                Spellweaver.getLOGGER().warn("[WriteSpellStickC2SPacket] 主手不是法杖！");
                return;
            }


            // 构建法术对象并写入法杖 NBT
            CompoundTag tag = new CompoundTag();
            tag.putString("name", spellName);
            tag.put("sequence",spellTag);
            CompoundTag wandTag = wandStack.getOrCreateTag();
            wandTag.put("stickSpell", tag);

            Spellweaver.getLOGGER().debug("[WriteSpellStickC2SPacket] 已将法术 {} 注入法杖", spellName);
        });
        return true;
    }
}