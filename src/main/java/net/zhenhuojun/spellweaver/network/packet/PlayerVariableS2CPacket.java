package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.capability.impl.long_term_variables.PlayerLongTermVariablesData;
import net.zhenhuojun.spellweaver.client.gui.util.ClientPlayerVariableData;


import java.util.function.Supplier;

public class PlayerVariableS2CPacket {
    private CompoundTag variableTag;

    public PlayerVariableS2CPacket(CompoundTag variableTag){
        this.variableTag=variableTag;
    }
    public PlayerVariableS2CPacket(FriendlyByteBuf buf){
        this.variableTag=buf.readNbt();
    }
    public void toByte(FriendlyByteBuf buf){
        buf.writeNbt(variableTag);
    }
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            PlayerLongTermVariablesData playerLongTermVariablesData=new PlayerLongTermVariablesData();
            playerLongTermVariablesData.loadNBT(variableTag);
            ClientPlayerVariableData.setPlayerLongTermVariablesData(playerLongTermVariablesData);
        });
        return true;
    }
}
