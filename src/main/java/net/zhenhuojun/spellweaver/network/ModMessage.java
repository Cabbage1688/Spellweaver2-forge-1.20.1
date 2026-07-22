package net.zhenhuojun.spellweaver.network;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.network.packet.*;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

public class ModMessage {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;

    private static int id(){
        return packetId++;
    }

    public static void register() {
        SimpleChannel net =
                NetworkRegistry.ChannelBuilder
                .named(fromNamespaceAndPath(Spellweaver.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(SpellCastingC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(SpellCastingC2SPacket::new)
                .encoder(SpellCastingC2SPacket::toByte)
                .consumerMainThread(SpellCastingC2SPacket::handle)
                .add();

        net.messageBuilder(ManaChangeS2CPacket.class,id(),NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ManaChangeS2CPacket::new)
                .encoder(ManaChangeS2CPacket::toByte)
                .consumerMainThread(ManaChangeS2CPacket::handle)
                .add();

        net.messageBuilder(BindSpellC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(BindSpellC2SPacket::new)
                .encoder(BindSpellC2SPacket::toByte)
                .consumerMainThread(BindSpellC2SPacket::handle)
                .add();

        net.messageBuilder(SpellStorageC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(SpellStorageC2SPacket::new)
                .encoder(SpellStorageC2SPacket::toByte)
                .consumerMainThread(SpellStorageC2SPacket::handle)
                .add();

        net.messageBuilder(SpellStorageSyncS2CPacket.class,id(),NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SpellStorageSyncS2CPacket::new)
                .encoder(SpellStorageSyncS2CPacket::toByte)
                .consumerMainThread(SpellStorageSyncS2CPacket::handle)
                .add();

        net.messageBuilder(DeleteSpellC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(DeleteSpellC2SPacket::new)
                .encoder(DeleteSpellC2SPacket::toByte)
                .consumerMainThread(DeleteSpellC2SPacket::handle)
                .add();

        net.messageBuilder(RenameSpellC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(RenameSpellC2SPacket::new)
                .encoder(RenameSpellC2SPacket::toByte)
                .consumerMainThread(RenameSpellC2SPacket::handle)
                .add();

        net.messageBuilder(CastSpellBySlotC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(CastSpellBySlotC2SPacket::new)
                .encoder(CastSpellBySlotC2SPacket::toByte)
                .consumerMainThread(CastSpellBySlotC2SPacket::handle)
                .add();

        net.messageBuilder(SpellStorageRespondS2CPacket.class,id(),NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SpellStorageRespondS2CPacket::new)
                .encoder(SpellStorageRespondS2CPacket::toByte)
                .consumerMainThread(SpellStorageRespondS2CPacket::handle)
                .add();

        net.messageBuilder(TeleportParticleS2CPacket.class,id(),NetworkDirection.PLAY_TO_CLIENT)
                .decoder(TeleportParticleS2CPacket::new)
                .encoder(TeleportParticleS2CPacket::toByte)
                .consumerMainThread(TeleportParticleS2CPacket::handle)
                .add();

        net.messageBuilder(UpdateSpellC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(UpdateSpellC2SPacket::new)
                .encoder(UpdateSpellC2SPacket::toBytes)
                .consumerMainThread(UpdateSpellC2SPacket::handle)
                .add();

        net.messageBuilder(PlayerVariableS2CPacket.class,id(),NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PlayerVariableS2CPacket::new)
                .encoder(PlayerVariableS2CPacket::toByte)
                .consumerMainThread(PlayerVariableS2CPacket::handle)
                .add();

        net.messageBuilder(OverloadDataS2CPacket.class,id(),NetworkDirection.PLAY_TO_CLIENT)
                .decoder(OverloadDataS2CPacket::new)
                .encoder(OverloadDataS2CPacket::toByte)
                .consumerMainThread(OverloadDataS2CPacket::handle)
                .add();

        net.messageBuilder(OverloadDataC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(OverloadDataC2SPacket::new)
                .encoder(OverloadDataC2SPacket::toByte)
                .consumerMainThread(OverloadDataC2SPacket::handle)
                .add();

        net.messageBuilder(WriteScrollC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(WriteScrollC2SPacket::new)
                .encoder(WriteScrollC2SPacket::toByte)
                .consumerMainThread(WriteScrollC2SPacket::handle)
                .add();

        net.messageBuilder(ScrollSpellCastingC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(ScrollSpellCastingC2SPacket::new)
                .encoder(ScrollSpellCastingC2SPacket::toByte)
                .consumerMainThread(ScrollSpellCastingC2SPacket::handle)
                .add();

        net.messageBuilder(WriteSpellStickC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(WriteSpellStickC2SPacket::new)
                .encoder(WriteSpellStickC2SPacket::toByte)
                .consumerMainThread(WriteSpellStickC2SPacket::handle)
                .add();

        net.messageBuilder(ClearSpellStickC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(ClearSpellStickC2SPacket::new)
                .encoder(ClearSpellStickC2SPacket::toByte)
                .consumerMainThread(ClearSpellStickC2SPacket::handle)
                .add();

        net.messageBuilder(ManaBottleC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(ManaBottleC2SPacket::new)
                .encoder(ManaBottleC2SPacket::toByte)
                .consumerMainThread(ManaBottleC2SPacket::handle)
                .add();

        net.messageBuilder(SpellMachineWriteC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(SpellMachineWriteC2SPacket::new)
                .encoder(SpellMachineWriteC2SPacket::toByte)
                .consumerMainThread(SpellMachineWriteC2SPacket::handle)
                .add();

        net.messageBuilder(ManaBallEffectS2CPacket.class,id(),NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ManaBallEffectS2CPacket::new)
                .encoder(ManaBallEffectS2CPacket::toBytes)
                .consumerMainThread(ManaBallEffectS2CPacket::handle)
                .add();

        net.messageBuilder(VoidErosionS2CPacket.class,id(),NetworkDirection.PLAY_TO_CLIENT)
                .decoder(VoidErosionS2CPacket::new)
                .encoder(VoidErosionS2CPacket::toBytes)
                .consumerMainThread(VoidErosionS2CPacket::handle)
                .add();

        net.messageBuilder(SpreadReactionS2CPacket.class,id(),NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SpreadReactionS2CPacket::new)
                .encoder(SpreadReactionS2CPacket::toBytes)
                .consumerMainThread(SpreadReactionS2CPacket::handle)
                .add();

        net.messageBuilder(ReactionEffectS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ReactionEffectS2CPacket::new)
                .encoder(ReactionEffectS2CPacket::toBytes)
                .consumerMainThread(ReactionEffectS2CPacket::handle)
                .add();

        net.messageBuilder(WriteSpellInScrollC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(WriteSpellInScrollC2SPacket::new)
                .encoder(WriteSpellInScrollC2SPacket::toByte)
                .consumerMainThread(WriteSpellInScrollC2SPacket::handle)
                .add();

        net.messageBuilder(OpenSpellNamingS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(OpenSpellNamingS2CPacket::new)
                .encoder(OpenSpellNamingS2CPacket::toByte)
                .consumerMainThread(OpenSpellNamingS2CPacket::handle)
                .add();

        net.messageBuilder(RayS2CPacket.class,id(),NetworkDirection.PLAY_TO_CLIENT)
                .decoder(RayS2CPacket::new)
                .encoder(RayS2CPacket::toBytes)
                .consumerMainThread(RayS2CPacket::handle)
                .add();

        net.messageBuilder(UnbindSpellC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(UnbindSpellC2SPacket::new)
                .encoder(UnbindSpellC2SPacket::toByte)
                .consumerMainThread(UnbindSpellC2SPacket::handle)
                .add();

        net.messageBuilder(UpdateSpellAuthorsC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(UpdateSpellAuthorsC2SPacket::new)
                .encoder(UpdateSpellAuthorsC2SPacket::toByte)
                .consumerMainThread(UpdateSpellAuthorsC2SPacket::handle)
                .add();

        net.messageBuilder(ImportSpellC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ImportSpellC2SPacket::new)
                .encoder(ImportSpellC2SPacket::toByte)
                .consumerMainThread(ImportSpellC2SPacket::handle)
                .add();

        net.messageBuilder(UpdateSpellNoteC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(UpdateSpellNoteC2SPacket::new)
                .encoder(UpdateSpellNoteC2SPacket::toByte)
                .consumerMainThread(UpdateSpellNoteC2SPacket::handle)
                .add();


        net.messageBuilder(ManaShieldChangeS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ManaShieldChangeS2CPacket::new)
                .encoder(ManaShieldChangeS2CPacket::toByte)
                .consumerMainThread(ManaShieldChangeS2CPacket::handle)
                .add();

        net.messageBuilder(PurifyEffectS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PurifyEffectS2CPacket::new)
                .encoder(PurifyEffectS2CPacket::toBytes)
                .consumerMainThread(PurifyEffectS2CPacket::handle)
                .add();

        net.messageBuilder(FeatherPenSpellC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(FeatherPenSpellC2SPacket::new)
                .encoder(FeatherPenSpellC2SPacket::toByte)
                .consumerMainThread(FeatherPenSpellC2SPacket::handle)
                .add();

        net.messageBuilder(MagicBrushClearC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(MagicBrushClearC2SPacket::new)
                .encoder(MagicBrushClearC2SPacket::toByte)
                .consumerMainThread(MagicBrushClearC2SPacket::handle)
                .add();

        net.messageBuilder(SpellBlockSyncS2CPacket.class,id(),NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SpellBlockSyncS2CPacket::new)
                .encoder(SpellBlockSyncS2CPacket::toByte)
                .consumerMainThread(SpellBlockSyncS2CPacket::handle)
                .add();

        net.messageBuilder(InscriptionTableWriteC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(InscriptionTableWriteC2SPacket::new)
                .encoder(InscriptionTableWriteC2SPacket::toByte)
                .consumerMainThread(InscriptionTableWriteC2SPacket::handle)
                .add();

        net.messageBuilder(InscriptionTableClearC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(InscriptionTableClearC2SPacket::new)
                .encoder(InscriptionTableClearC2SPacket::toByte)
                .consumerMainThread(InscriptionTableClearC2SPacket::handle)
                .add();

        net.messageBuilder(CancelAllSpellsC2SPacket.class,id(),NetworkDirection.PLAY_TO_SERVER)
                .decoder(CancelAllSpellsC2SPacket::new)
                .encoder(CancelAllSpellsC2SPacket::toByte)
                .consumerMainThread(CancelAllSpellsC2SPacket::handle)
                .add();


    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
    public static <MSG> void sendToClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }

    //这个方法按维度发送，用于法术方块，不同Level数据不一样
    public static <MSG> void sendToClientsInLevel(MSG message, ServerLevel level) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() == level) {
                INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
            }
        }
    }
}
