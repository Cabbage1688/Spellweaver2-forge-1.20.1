package net.zhenhuojun.spellweaver.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.client.gui.SpellNamingScreen;
import net.zhenhuojun.spellweaver.client.gui.item_gui.SpellStickEditScreen;
import net.zhenhuojun.spellweaver.client.gui.util.ClientPlayerSpellData;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.ClearSpellStickC2SPacket;
import net.zhenhuojun.spellweaver.network.packet.ScrollSpellCastingC2SPacket;
import net.zhenhuojun.spellweaver.network.packet.WriteScrollC2SPacket;
import net.zhenhuojun.spellweaver.network.packet.WriteSpellStickC2SPacket;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;
import net.zhenhuojun.spellweaver.spell.util.RunesExecuteMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpellStickItem extends SwordItem {
   // private static final int ATTACK_COOLDOWN_TICKS = 20;


    public SpellStickItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);


        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("stickSpell")) {
            CompoundTag spellData = tag.getCompound("stickSpell");
            appendSpellHoverText(spellData, tooltip);
        } else {
            tooltip.add(Component.literal("法杖未注入法术")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    public static void appendSpellHoverText(CompoundTag spellData, List<Component> tooltipComponents) {
        if (spellData.contains("name")) {
            String spellName = spellData.getString("name");
            tooltipComponents.add(
                    Component.literal("法术")
                            .append(": ")
                            .append(Component.literal(spellName))
                            .withStyle(ChatFormatting.AQUA)
            );
        }
    }
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 造成基础伤害
        boolean attacked = super.hurtEnemy(stack, target, attacker);
        //if (attacked && attacker instanceof Player player) {
            // 设置玩家的攻击冷却。我发现这里tm是物品冷却不是攻击冷却，所以不要了
           // player.getCooldowns().addCooldown(this, ATTACK_COOLDOWN_TICKS);
        //}
        return attacked;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand){
        Spellweaver.getLOGGER().debug("[Spellweaver:SpellStickItem/use方法]方法触发");
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrCreateTag();

        // 判断当前手持位置
        boolean isMainHand = (hand == InteractionHand.MAIN_HAND);
        boolean hasSpell = tag.contains("stickSpell");

        if (level.isClientSide) {
            if (isMainHand) {
                if (!hasSpell) {

                    Minecraft.getInstance().setScreen(new SpellStickEditScreen());
                } else {
                    // 主手 + 有法术 → 释放法术
                    CompoundTag spellData = tag.getCompound("stickSpell");
                    if (spellData != null && spellData.contains("sequence")) {
                        ModMessage.sendToServer(new ScrollSpellCastingC2SPacket(spellData.getCompound("sequence"), "stick"));
                    }
                }
            } else {
                // 副手 + 有法术 → 删除法术
                if (hasSpell) {
                     ModMessage.sendToServer(new ClearSpellStickC2SPacket());
                }
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static final Tier STAFF_TIER = new Tier() {
        @Override
        public int getUses() {
            return 36760; // 耐久度，钻石剑为1561
        }

        @Override
        public float getSpeed() {
            return 4.0F; // 挖掘速度，
        }

        @Override
        public float getAttackDamageBonus() {
            return 5.0F; // 额外攻击加成
        }

        @Override
        public int getLevel() {
            return 2; // 挖掘等级，2相当于铁工具
        }

        @Override
        public int getEnchantmentValue() {
            return 0; // 附魔能力，18高于铁(14)
        }

        @Override
        public @NotNull Ingredient getRepairIngredient() {
            return Ingredient.of(Items.GOLD_INGOT);
        }

    };

    /*@Override//TODO 我觉得这里可以写点粒子？
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!entity.level().isClientSide) {

        }
        return true; // 返回 true 表示不再调用后续更新
    }

     */
    // 附魔光效：有法术时发光
    @Override
    public boolean isFoil(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("stickSpell");
    }

    // 法术存储方法
    private void storeCurrentSpell(SequenceNode sequenceNode) {
        // 获取法术名称（可扩展为自定义命名）
        String spellName = "法术_" + System.currentTimeMillis();
        // 打开命名对话框
        Minecraft.getInstance().setScreen(new SpellNamingScreen(
                spellName,
                name -> confirmSpellStorage(name, sequenceNode.serializeNBT())
        ));
    }
    // 确认存储回调
    private void confirmSpellStorage(String spellName, CompoundTag spellTag) {
        ModMessage.sendToServer(new WriteSpellStickC2SPacket(spellName,spellTag));
        Minecraft.getInstance().setScreen(null);
    }
}
