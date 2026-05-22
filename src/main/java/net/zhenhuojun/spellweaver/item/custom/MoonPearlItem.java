package net.zhenhuojun.spellweaver.item.custom;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zhenhuojun.spellweaver.capability.impl.mana.ManaUtil;

public class MoonPearlItem extends Item {

    public MoonPearlItem(Properties pProperties) {
        super(pProperties);
    }
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand){
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        if(!pLevel.isClientSide) {
            ManaUtil.addManaExpOrAwakeManaByMoonPearlAndSendPacket((ServerPlayer)pPlayer);
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
    }

}
