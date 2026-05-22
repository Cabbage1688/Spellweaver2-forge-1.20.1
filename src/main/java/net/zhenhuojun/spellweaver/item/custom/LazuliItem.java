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
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.client.gui.item_gui.ScrollViewScreen;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.ScrollSpellCastingC2SPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LazuliItem extends Item {

    public LazuliItem(Properties properties) {
        super(properties.durability(512));
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 从 NBT 中获取法术信息
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("scrollSpell")) {
            CompoundTag spellData = tag.getCompound("scrollSpell");
            appendSpellHoverText(spellData, tooltip);
        } else {
            // 如果没有法术数据，显示空卷轴提示
            tooltip.add(Component.literal("卷轴为空")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    public static void appendSpellHoverText(CompoundTag spellData, List<Component> tooltipComponents) {
        // 显示法术名称
        if (spellData.contains("name")) {
            String spellName = spellData.getString("name");
            tooltipComponents.add(
                    Component.literal("卷轴法术")
                            .append(": ")
                            .append(Component.literal(spellName))
                            .withStyle(ChatFormatting.AQUA)
            );
        }
        //魔力数值显示
        if(spellData.contains("mana")){
            double mana=spellData.getDouble("mana");
            tooltipComponents.add(
                    Component.literal("魔力")
                            .append(": ")
                            .append(String.valueOf(mana))
                            .withStyle(ChatFormatting.LIGHT_PURPLE)
            );
        }else{
            tooltipComponents.add(
                    Component.literal("未充能").withStyle(ChatFormatting.LIGHT_PURPLE)
            );
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        //Spellweaver.getLOGGER().debug("[Spellweaver:ScrollItem/use方法]use方法调用");
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
        // 当满足自定义条件（如魔力值大于0）时返回true，激活光效渲染
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
