package net.zhenhuojun.spellweaver.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.zhenhuojun.spellweaver.client.gui.item_gui.ScrollViewScreen;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.ScrollSpellCastingC2SPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class UsedScroll extends Item {

    protected UsedScroll(Properties properties, int maxDurability) {
        super(properties.durability(maxDurability));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("scrollSpell")) {
            CompoundTag spellData = tag.getCompound("scrollSpell");
            appendSpellHoverText(spellData, tooltip);
        } else {
            tooltip.add(Component.literal("卷轴为空")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    protected static void appendSpellHoverText(CompoundTag spellData, List<Component> tooltipComponents) {
        if (spellData.contains("name")) {
            String spellName = spellData.getString("name");
            tooltipComponents.add(
                    Component.literal("卷轴法术")
                            .append(": ")
                            .append(Component.literal(spellName))
                            .withStyle(ChatFormatting.AQUA)
            );
        }
        if (spellData.contains("mana")) {
            double mana = spellData.getDouble("mana");
            tooltipComponents.add(
                    Component.literal("魔力")
                            .append(": ")
                            .append(String.valueOf(mana))
                            .withStyle(ChatFormatting.LIGHT_PURPLE)
            );
        } else {
            tooltipComponents.add(
                    Component.literal("未充能").withStyle(ChatFormatting.LIGHT_PURPLE)
            );
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            if (player.isCrouching()) {
                Minecraft.getInstance().setScreen(new ScrollViewScreen(stack, level));
                return InteractionResultHolder.sidedSuccess(stack, true);
            }
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        // 服务端逻辑
        if (player.isCrouching()) {
            return InteractionResultHolder.sidedSuccess(stack, false);
        }
        CompoundTag aTag = stack.getTag();
        if (aTag != null && aTag.contains("scrollSpell")) {
            CompoundTag spellData = aTag.getCompound("scrollSpell");
            if (spellData.contains("sequence")) {
                ModMessage.sendToServer(new ScrollSpellCastingC2SPacket(
                        spellData.getCompound("sequence"), "scroll"
                ));
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("scrollSpell")) {
            CompoundTag spellData = tag.getCompound("scrollSpell");
            if (spellData.contains("mana")) {
                return spellData.getDouble("mana") > 0;
            }
        }
        return false;
    }
}