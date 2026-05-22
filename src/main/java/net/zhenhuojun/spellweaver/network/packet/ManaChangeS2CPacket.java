package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.client.gui.util.ClientPlayerManaData;

import java.util.function.Supplier;
//恕我直言，1.20.1forge的网络系统没有1.20.4neoForge好用,奶奶个腿的数据还要我自己缓存在包里
public class ManaChangeS2CPacket {
    private double mana;
    private int maxMana;
    private int manaLevel;

    public ManaChangeS2CPacket(double mana,int maxMana,int manaLevel){
        this.mana=mana;
        this.maxMana=maxMana;
        this.manaLevel=manaLevel;
    }
    public ManaChangeS2CPacket(FriendlyByteBuf buf){
        this.mana=buf.readDouble();
        this.maxMana=buf.readInt();
        this.manaLevel=buf.readInt();
    }
    public void toByte(FriendlyByteBuf buf){
         buf.writeDouble(mana);
         buf.writeInt(maxMana);
         buf.writeInt(manaLevel);
    }
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientPlayerManaData.set(mana);
            ClientPlayerManaData.setMaxMana(maxMana);
            ClientPlayerManaData.setManaLevel(manaLevel);
        });
        return true;
    }
}
