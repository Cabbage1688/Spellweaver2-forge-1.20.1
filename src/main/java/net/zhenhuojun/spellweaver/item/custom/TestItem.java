package net.zhenhuojun.spellweaver.item.custom;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerManaProvider;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.ManaChangeS2CPacket;
import net.zhenhuojun.spellweaver.spell.element.ElementType;
import net.zhenhuojun.spellweaver.spell.node.LoopNode;
import net.zhenhuojun.spellweaver.spell.node.NormalNode;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;
import net.zhenhuojun.spellweaver.spell.util.RunesExecuteMethod;

import java.util.ArrayList;
import java.util.List;

import static net.zhenhuojun.spellweaver.spell.element.Element.applyElement;

public class TestItem extends Item {

    public TestItem(Properties pProperties) {
        super(pProperties);
    }

    @Override//非常丑陋的逻辑，但是至少起到了测试作用不是吗，这个方法在客户端和服务端均有调用
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand){
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        if(!pLevel.isClientSide){

            pPlayer.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
                mana.addMana(mana.getMaxMana()-mana.getMana());
                ModMessage.sendToPlayer(new ManaChangeS2CPacket(mana.getMana(), mana.getMaxMana()
                        ,mana.getMana_level(),mana.getMana_exp(), mana.getPresent_exp()), (ServerPlayer) pPlayer);
            });
            //测试粒子是否正常工作
            applyElement(pPlayer, ElementType.randomElement(), 100);

        }/*else{
            Spellweaver.getLOGGER().debug("[Spellweaver]坏了，怎么是客户端");
        }
        */
        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
    }

}
