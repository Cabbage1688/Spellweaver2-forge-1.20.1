package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.client.gui.util.ClientPlayerManaData;

import java.util.function.Supplier;
public class ManaChangeS2CPacket {
    private double mana;
    private int maxMana;
    private int manaLevel;
    private long manaExp;
    private long currentExp;

    public ManaChangeS2CPacket(double mana,int maxMana,int manaLevel,long manaExp,long currentExp){
        this.mana=mana;
        this.maxMana=maxMana;
        this.manaLevel=manaLevel;
        this.manaExp=manaExp;
        this.currentExp=currentExp;
    }
    public ManaChangeS2CPacket(FriendlyByteBuf buf){
        this.mana=buf.readDouble();
        this.maxMana=buf.readInt();
        this.manaLevel=buf.readInt();
        this.manaExp=buf.readLong();
        this.currentExp=buf.readLong();
    }
    public void toByte(FriendlyByteBuf buf){
         buf.writeDouble(mana);
         buf.writeInt(maxMana);
         buf.writeInt(manaLevel);
         buf.writeLong(manaExp);
         buf.writeLong(currentExp);
    }
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientPlayerManaData.set(mana);
            ClientPlayerManaData.setMaxMana(maxMana);
            ClientPlayerManaData.setManaLevel(manaLevel);
            ClientPlayerManaData.setManaExp(manaExp);
            ClientPlayerManaData.setCurrentExp(currentExp);
        });
        return true;
    }
}
