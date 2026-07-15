package net.zhenhuojun.spellweaver.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.zhenhuojun.spellweaver.client.gui.util.ClientPlayerSpellData;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.FeatherPenSpellC2SPacket;
import org.jetbrains.annotations.NotNull;

public class FeatherPen extends Item {

    public FeatherPen(Properties pProperties) {
        super(pProperties);
    }

    //TODO可以考虑重写useON方法来处理方块逻辑

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 客户端执行发送逻辑
        if (level.isClientSide) {
            ClientPlayerSpellData data = ClientPlayerSpellData.get(player);
            CompoundTag spellTag = data.getSpellTag();
            if (spellTag == null || spellTag.isEmpty()) {
                player.sendSystemMessage(Component.literal("§c你还没有写任何法术！"));
                return InteractionResultHolder.fail(stack);
            }

            boolean writeToBlock = player.isCrouching();
            BlockPos targetPos = null;
            if (writeToBlock) {
                // 获取准星所指的方块（距离3格）
                HitResult hit = player.pick(3.0, 0.0F, false);
                if (hit.getType() != HitResult.Type.BLOCK) {
                    player.sendSystemMessage(Component.literal("§c未对准任何方块！"));
                    return InteractionResultHolder.fail(stack);
                }
                targetPos = ((BlockHitResult) hit).getBlockPos();
            }


            ModMessage.sendToServer(new FeatherPenSpellC2SPacket(spellTag, writeToBlock, targetPos));

            return InteractionResultHolder.success(stack);
        }

        // 服务端直接返回成功
        return InteractionResultHolder.success(stack);
    }
}
