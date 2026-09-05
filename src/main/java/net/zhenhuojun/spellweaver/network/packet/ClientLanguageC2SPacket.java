package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.loot.LanguageTracker;

import java.util.function.Supplier;

/**
 * 客户端发送自身语言代码到服务端，供战利品系统语言感知使用。
 */
public class ClientLanguageC2SPacket {
    private final String language;

    public ClientLanguageC2SPacket(String language) {
        this.language = language;
    }

    public ClientLanguageC2SPacket(FriendlyByteBuf buf) {
        this.language = buf.readUtf();
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeUtf(language);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                LanguageTracker.setLanguage(player.getUUID(), language);
            }
        });
        return true;
    }
}
