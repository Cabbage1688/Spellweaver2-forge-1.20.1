package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.item.custom.LazuliBeforeItem;
import net.zhenhuojun.spellweaver.item.custom.ScrollBeforeItem;

import java.util.function.Supplier;

public class WriteSpellInScrollC2SPacket {
    private final CompoundTag spellTag;

    public WriteSpellInScrollC2SPacket(CompoundTag tag) {
        this.spellTag = tag;
    }

    public WriteSpellInScrollC2SPacket(FriendlyByteBuf buf) {
        this.spellTag = buf.readNbt();
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeNbt(spellTag);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            // 查找玩家手中的卷轴
            ItemStack stack = player.getMainHandItem();
            //if (!(stack.getItem() instanceof LazuliBeforeItem)) {
               // stack = player.getOffhandItem();
                //if (!(stack.getItem() instanceof LazuliBeforeItem)) {
                  //  return;
                //}
            //}
            if(!(stack.getItem() instanceof LazuliBeforeItem ||stack.getItem() instanceof ScrollBeforeItem)) return;
            // 将法术写入卷轴
            stack.getOrCreateTag().put("sequence", spellTag);
            player.getInventory().setChanged();  // 标记物品已更改
        });
        return true;
    }
}
