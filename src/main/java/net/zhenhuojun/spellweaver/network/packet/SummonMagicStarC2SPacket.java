package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.capability.impl.mana.ManaUtil;
import net.zhenhuojun.spellweaver.entity.ModEntities;
import net.zhenhuojun.spellweaver.entity.impl.MagicStarEntity;
import net.zhenhuojun.spellweaver.network.ModMessage;

import java.util.function.Supplier;

/**
 * 客户端→服务器：请求召唤魔法之星实体。
 * 服务端校验：魔力等级>=100 且 当前魔力>=5000，满足后消耗5000魔力并在玩家头顶生成实体。
 */
public class SummonMagicStarC2SPacket {

    public SummonMagicStarC2SPacket() {
    }

    public SummonMagicStarC2SPacket(FriendlyByteBuf buf) {
    }

    public void toByte(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (!(player.level() instanceof ServerLevel serverLevel)) return;

            // 校验魔力等级>=100
            final int[] manaLevel = {0};
            player.getCapability(net.zhenhuojun.spellweaver.capability.provider.mana.PlayerManaProvider.PLAYER_MANA)
                    .ifPresent(pm -> manaLevel[0] = pm.getMana_level());
            if (manaLevel[0] < 100) {
                player.sendSystemMessage(Component.translatable("message.spellweaver.magic_star.level_too_low")
                        .withStyle(net.minecraft.ChatFormatting.RED));
                return;
            }

            // 校验并消耗5000魔力
            if (!ManaUtil.subManaAndAddExpAndSendPacket(5000, player)) {
                player.sendSystemMessage(Component.translatable("message.spellweaver.not_enough_mana")
                        .withStyle(net.minecraft.ChatFormatting.RED));
                return;
            }

            // 在玩家头顶生成魔法之星实体
            MagicStarEntity magicStar = new MagicStarEntity(ModEntities.MAGIC_STAR.get(), serverLevel, player);
            Vec3 spawnPos = player.getEyePosition().add(0.0, 0.5, 0.0);
            magicStar.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            serverLevel.addFreshEntity(magicStar);
        });
        return true;
    }
}
