package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.StoredSpell;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerSpellStorageProvider;
import net.zhenhuojun.spellweaver.spell.util.RunesExecuteMethod;

import java.util.function.Supplier;

public class CastSpellBySlotC2SPacket {
    private final int slot;

    public CastSpellBySlotC2SPacket(int slot){
        this.slot=slot;
    }

    public CastSpellBySlotC2SPacket(FriendlyByteBuf buf){
        this.slot=buf.readInt();

    }
    public void toByte(FriendlyByteBuf buf){
        buf.writeInt(slot);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {

            ServerPlayer player = context.getSender();
            ServerLevel level = player.serverLevel();
            player.getCapability(PlayerSpellStorageProvider.PLAYER_SPELL_STORAGE).ifPresent(playerSpellStorage -> {
                StoredSpell spell= playerSpellStorage.getSpellInSlot(slot).orElse(null);
                if(spell!=null){
                    RunesExecuteMethod.spellLogic(spell.getSequenceNode(),level,player);
                    Spellweaver.getLOGGER().debug("[Spellweaver:CastSpellBySlotC2SPacket/handle]法术不为空，调用法术执行逻辑，法术详情：{}",spell.getSequenceNode().serializeNBT());
                }
            });


        });
        return true;
    }
}
