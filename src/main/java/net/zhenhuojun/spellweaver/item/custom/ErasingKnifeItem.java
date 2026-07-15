package net.zhenhuojun.spellweaver.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.MagicBrushClearC2SPacket;
import org.jetbrains.annotations.NotNull;

public class ErasingKnifeItem extends Item {

    public ErasingKnifeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            boolean clearBlock = player.isCrouching();
            BlockPos targetPos = null;

            if (clearBlock) {
                HitResult hit = player.pick(3.0, 0.0F, false);
                if (hit.getType() != HitResult.Type.BLOCK) {
                    player.sendSystemMessage(Component.literal("§c未对准任何方块！"));
                    return InteractionResultHolder.fail(stack);
                }
                targetPos = ((BlockHitResult) hit).getBlockPos();
            } else {
                ItemStack offhand = player.getOffhandItem();
                if (offhand.isEmpty()) {
                    player.sendSystemMessage(Component.literal("§c你的副手没有物品！"));
                    return InteractionResultHolder.fail(stack);
                }
                if (offhand.getTag() != null && (!offhand.hasTag() || !offhand.getTag().contains("SpellData"))) {
                    player.sendSystemMessage(Component.literal("§c副手物品没有法术！"));
                    return InteractionResultHolder.fail(stack);
                }
            }

            ModMessage.sendToServer(new MagicBrushClearC2SPacket(clearBlock, targetPos));
            Spellweaver.getLOGGER().debug("[Spellweaver:ErasingKnifeItem/use]已发送法术清除包");
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.success(stack);
    }
}