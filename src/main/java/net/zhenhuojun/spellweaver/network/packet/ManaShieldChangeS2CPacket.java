package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.client.gui.util.ClientManaShieldData;

import java.util.function.Supplier;

public class ManaShieldChangeS2CPacket {
    private final boolean active;
    private final double shieldAmount;

    public ManaShieldChangeS2CPacket(boolean active, double shieldAmount) {
        this.active = active;
        this.shieldAmount = shieldAmount;
    }

    public ManaShieldChangeS2CPacket(FriendlyByteBuf buf) {
        this.active = buf.readBoolean();
        this.shieldAmount = buf.readDouble();
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeBoolean(active);
        buf.writeDouble(shieldAmount);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // 客户端更新数据
            ClientManaShieldData.setActive(active);
            ClientManaShieldData.setShieldAmount(shieldAmount);
        });
        context.setPacketHandled(true);
        return true;
    }
}