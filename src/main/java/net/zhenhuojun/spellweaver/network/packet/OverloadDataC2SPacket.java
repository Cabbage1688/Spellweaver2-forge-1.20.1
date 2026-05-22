package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerManaOverloadProvider;
import net.zhenhuojun.spellweaver.client.data_util.ClientPlayerOverloadData;

import java.util.function.Supplier;

public class OverloadDataC2SPacket {
    private boolean enabled;
    private int currentMultiplier;
    private int maxMultiplier;

    public OverloadDataC2SPacket(boolean enabled, int currentMultiplier, int maxMultiplier) {
        this.enabled = enabled;
        this.currentMultiplier = currentMultiplier;
        this.maxMultiplier = maxMultiplier;
    }

    public OverloadDataC2SPacket(FriendlyByteBuf buf) {
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
            ServerPlayer player = context.getSender();
            if(player!=null){
                player.getCapability(PlayerManaOverloadProvider.PLAYER_MANA_OVERLOAD).ifPresent(playerManaOverload -> {
                    playerManaOverload.setEnabled(enabled);
                    playerManaOverload.setCurrentMultiplier(currentMultiplier);
                    playerManaOverload.setMaxMultiplier(maxMultiplier);
                });
            }
        });
        return true;
    }
}
