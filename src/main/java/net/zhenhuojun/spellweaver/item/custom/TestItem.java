package net.zhenhuojun.spellweaver.item.custom;

import net.minecraft.resources.ResourceLocation;
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
import net.zhenhuojun.spellweaver.entity.ModEntities;
import net.zhenhuojun.spellweaver.entity.impl.MagicStarEntity;
import net.zhenhuojun.spellweaver.entity.impl.SpellEntity;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.ManaChangeS2CPacket;
import net.zhenhuojun.spellweaver.network.packet.PhotonEffectS2CPacket;
import net.zhenhuojun.spellweaver.spell.element.ElementType;
import net.zhenhuojun.spellweaver.spell.node.LoopNode;
import net.zhenhuojun.spellweaver.spell.node.NormalNode;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;
import net.zhenhuojun.spellweaver.spell.util.EffectType;
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
            /*pPlayer.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
                mana.addMana(mana.getMaxMana()-mana.getMana());
                ModMessage.sendToPlayer(new ManaChangeS2CPacket(mana.getMana(), mana.getMaxMana()
                        ,mana.getMana_level(),mana.getMana_exp(), mana.getPresent_exp()), (ServerPlayer) pPlayer);
            });
            //测试粒子是否正常工作
            applyElement(pPlayer, ElementType.randomElement(), 100);
            MagicStarEntity magicStarEntity=new MagicStarEntity(ModEntities.MAGIC_STAR.get(),pLevel,(ServerPlayer) pPlayer);
            magicStarEntity.setPos(pPlayer.getEyePosition());
            pLevel.addFreshEntity(magicStarEntity);
             */
            SpellEntity spellEntity=new SpellEntity(ModEntities.SPELL_ENTITY.get(),pLevel,100,new SequenceNode(),SpellEntity.TriggerMode.HIT,20,false);
            spellEntity.setPos(pPlayer.getEyePosition());
            int a=pLevel.random.nextInt(0,4);
            spellEntity.setDeltaMovement(pPlayer.getLookAngle().x*a,pPlayer.getLookAngle().y*a,pPlayer.getLookAngle().z*a);
            pLevel.addFreshEntity(spellEntity);
            ModMessage.sendToClients(new PhotonEffectS2CPacket(ResourceLocation.fromNamespaceAndPath("spellweaver","spell_entity3"),
                    pLevel, spellEntity, EffectType.INSTANT_DEAD_ENTITY_EFFECT));
            spellEntity.setMana(5);
        }else {
            Spellweaver.getLOGGER().debug("[Spellweaver/TestItem]客户端玩家速度{}",pPlayer.getDeltaMovement());
        }
        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
    }

}
