package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.client.data_util.ClientPlayerOverloadData;

import java.util.function.Supplier;

public class OverloadDataS2CPacket {
    private boolean enabled;
    private int currentMultiplier;
    private int maxMultiplier;

    public OverloadDataS2CPacket(boolean enabled, int currentMultiplier, int maxMultiplier) {
        this.enabled = enabled;
        this.currentMultiplier = currentMultiplier;
        this.maxMultiplier = maxMultiplier;
    }

    public OverloadDataS2CPacket(FriendlyByteBuf buf) {
        this.enabled = buf.readBoolean();
        this.currentMultiplier = buf.readInt();
        this.maxMultiplier = buf.readInt();
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeBoolean(enabled);
        buf.writeInt(currentMultiplier);
        buf.writeInt(maxMultiplier);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientPlayerOverloadData.setEnabled(enabled);
            ClientPlayerOverloadData.setCurrentMultiplier(currentMultiplier);
            ClientPlayerOverloadData.setMaxMultiplier(maxMultiplier);
        });
        return true;
    }
}
