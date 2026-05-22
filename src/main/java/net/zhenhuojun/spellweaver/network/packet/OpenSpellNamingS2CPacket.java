package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.client.gui.SpellNamingScreen;
import net.zhenhuojun.spellweaver.network.ModMessage;

import java.util.function.Supplier;

public class OpenSpellNamingS2CPacket {
    private final CompoundTag sequenceTag;

    public OpenSpellNamingS2CPacket(CompoundTag sequenceTag) {
        this.sequenceTag = sequenceTag;
    }

    public OpenSpellNamingS2CPacket(FriendlyByteBuf buf) {
        this.sequenceTag = buf.readNbt();
    }

    public void toByte(FriendlyByteBuf buf) {
        buf.writeNbt(sequenceTag);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            String defaultName = "法术_" + System.currentTimeMillis();
            Minecraft.getInstance().setScreen(new SpellNamingScreen(
                    defaultName,
                    name -> {
                        ModMessage.sendToServer(new WriteScrollC2SPacket(name,sequenceTag));
                        Minecraft.getInstance().setScreen(null);
                    }
            ));
        });
        return true;
    }
}
