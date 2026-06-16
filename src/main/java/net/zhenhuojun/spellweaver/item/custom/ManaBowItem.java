package net.zhenhuojun.spellweaver.item.custom;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.capability.impl.mana.ManaUtil;
import net.zhenhuojun.spellweaver.entity.ModEntities;
import net.zhenhuojun.spellweaver.entity.impl.ManaArrow;

public class ManaBowItem extends BowItem {
    private static final int MANA_COST_PER_SHOT = 5;          // 每次射击消耗魔力
    private static final double MANA_MAINTENANCE_COST = 0.05; // 每 tick 每把消耗

    public ManaBowItem(Properties pProperties) {
        super(pProperties);
    }

    private AbstractArrow createArrow(Level level, ItemStack bowStack,LivingEntity shooter) {
        // 使用自定义箭矢，携带法术列表
        ManaArrow arrow = new ManaArrow(ModEntities.MANA_ARROW.get(), level, bowStack);
        arrow.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
        arrow.setOwner(shooter);
        arrow.setBaseDamage(2.0);
        return arrow;
    }

    @Override//我已经在ManaArrow里写了绕过无敌帧的逻辑，箭矢伤害和法术伤害不会冲突
    public void releaseUsing(ItemStack stack, Level level, LivingEntity shooter, int timeCharged) {
        if (!(shooter instanceof ServerPlayer player)) return;
        int charge = getUseDuration(stack) - timeCharged;
        float power = getPowerForTime(charge);
        if (power < 0.1f) return;

        // 检查魔力
        if (!ManaUtil.subManaAndAddExpAndSendPacket( MANA_COST_PER_SHOT,player)) {
            return;
        }
        // 创建自定义箭矢，并传入弓的物品堆（以获取法术列表）
        AbstractArrow arrow = createArrow(level, stack,shooter);
        shootArrow(level, player, stack, arrow, power);
        Spellweaver.getLOGGER().debug("[Spellweaver:ManaBowItem/releaseUsing]箭矢力度{}",power);

    }

    public static float getPowerForTime(int pCharge) {
        float f = (float)pCharge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F&&f<=1.50F) {
            f=(float)pCharge / 20.0F;
        } else if (f>1.50F) {
            f=1.5F;
        }
        return f;
    }


    private void shootArrow(Level level, Player player, ItemStack bowStack, AbstractArrow arrow, float power) {
        if (arrow == null) return;

        // 设置射击精度
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
        if (power >= 1.0F) {
            arrow.setCritArrow(true);
        }

        // 应用附魔效果
        int powerEnchant = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, bowStack);
        if (powerEnchant > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() + (double) powerEnchant * 0.5 + 0.5);
        }
        int punchEnchant = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, bowStack);
        if (punchEnchant > 0) {
            arrow.setKnockback(punchEnchant);
        }
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, bowStack) > 0) {
            arrow.setSecondsOnFire(100);
        }

        level.addFreshEntity(arrow);
        // 不消耗箭矢
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            Spellweaver.getLOGGER().debug("Client: use called");
        }
        ItemStack stack = player.getItemInHand(hand);
        //调用原版 use，开始拉弓
        //return super.use(level, player, hand);

        ItemStack itemstack = player.getItemInHand(hand);
        boolean flag = true;

        InteractionResultHolder<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, level, player, hand, flag);
        if (ret != null) return ret;

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemstack);
    }


    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!entity.level().isClientSide) {
            entity.discard();
        }
        return true; // 返回 true 表示不再调用后续更新
    }

}
