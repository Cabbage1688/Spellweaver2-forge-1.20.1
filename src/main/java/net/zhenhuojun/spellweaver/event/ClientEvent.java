package net.zhenhuojun.spellweaver.event;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.block.ModBlockEntities;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.StoredSpell;
import net.zhenhuojun.spellweaver.client.data_util.ClientPlayerOverloadData;
import net.zhenhuojun.spellweaver.client.gui.SpellWeavingScreen;
import net.zhenhuojun.spellweaver.client.gui.util.ClientPlayerManaData;
import net.zhenhuojun.spellweaver.client.gui.util.ClientPlayerStorageData;
import net.zhenhuojun.spellweaver.client.hud.ManaHud;
import net.zhenhuojun.spellweaver.client.render.impl.*;
import net.zhenhuojun.spellweaver.entity.ModEntities;
import net.zhenhuojun.spellweaver.entity.impl.ManaArrow;
import net.zhenhuojun.spellweaver.entity.impl.SpellEffectEntity;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.key.KeyBinding;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.OverloadDataC2SPacket;
import net.zhenhuojun.spellweaver.network.packet.SpellCastingC2SPacket;

import javax.swing.text.JTextComponent;

import static net.zhenhuojun.spellweaver.key.KeyBinding.*;

public class ClientEvent {
    @Mod.EventBusSubscriber(modid = Spellweaver.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent//此处为按键的逻辑，当某个按键被按下则触发对应的逻辑
        public static void onKeyInput(InputEvent.Key event) {
            if(KeyBinding.TEST_KEY.consumeClick()){
               // Minecraft.getInstance().player.sendSystemMessage(Component.literal("Pressed Test Key!"));
                Minecraft.getInstance().setScreen(new SpellWeavingScreen(Component.literal("测试")));
            }
            for (int slot = 0; slot < SPELL_KEYS.length; slot++) {
                if (SPELL_KEYS[slot].consumeClick()) {
                    // 发送施法请求
                    /**
                     * 这tm就很傻逼，我也不知道为什么用注释起来的代码会导致快捷键进世界之后只能用一次，后续就不执行法术
                     * 但他妈的日志又显示所有的环节都是正常的，法术数据获取无误就是不执行
                     * 严重怀疑是forge本身的问题......
                     */
                   // ModMessage.sendToServer(new CastSpellBySlotC2SPacket(slot));
                    //Spellweaver.getLOGGER().debug("[Spellweaver:ClientEvent/ClientForgeEvents/onKeyInput]发送施法请求，槽位为{}",slot);
                    StoredSpell spell =ClientPlayerStorageData.getPlayerSpellStorage().getSpellInSlot(slot).orElse(null);
                    if(spell!=null){
                        ModMessage.sendToServer(new SpellCastingC2SPacket(spell.getSequenceNode().serializeNBT()));
                    }
                }
            }
            Player player=Minecraft.getInstance().player;
            if(player==null) return;
            if (OVERLOAD_KEY.consumeClick()) {
                if(player!=null){
                    if(ClientPlayerManaData.getManaLevel()<10) return;
                    if(ClientPlayerOverloadData.isEnabled()){
                        ClientPlayerOverloadData.setEnabled(false);
                        player.displayClientMessage(
                                Component.literal("法术超载关闭").withStyle(ChatFormatting.LIGHT_PURPLE),
                                true
                        );
                    } else {
                        ClientPlayerOverloadData.setEnabled(true);
                        player.displayClientMessage(
                                Component.literal("法术超载启用,超载倍数"+ClientPlayerOverloadData.getCurrentMultiplier()).withStyle(ChatFormatting.LIGHT_PURPLE),
                                true
                        );

                       /* if (player.level().isClientSide) {
                            SpellEffectEntity entity = new SpellEffectEntity(ModEntities.SPELL_EFFECT.get(), player.level());
                            entity.setPos(player.getX(), player.getY() + 1.2, player.getZ());
                            player.level().addFreshEntity(entity);
                        }

                        */
                    }
                    ModMessage.sendToServer(new OverloadDataC2SPacket(ClientPlayerOverloadData.isEnabled(),
                            ClientPlayerOverloadData.getCurrentMultiplier(),ClientPlayerOverloadData.getMaxMultiplier()));
                }
            }else if (!ClientPlayerOverloadData.isEnabled()&&OVERLOAD_UP_KEY.consumeClick()) {
                    ClientPlayerOverloadData.addCurrentMultiplier(1);
                    player.displayClientMessage(
                            Component.literal("超载倍数调整为"+ClientPlayerOverloadData.getCurrentMultiplier()).withStyle(ChatFormatting.LIGHT_PURPLE),
                            true
                    );
                    ModMessage.sendToServer(new OverloadDataC2SPacket(ClientPlayerOverloadData.isEnabled(),
                            ClientPlayerOverloadData.getCurrentMultiplier(),ClientPlayerOverloadData.getMaxMultiplier()));
            } else if (!ClientPlayerOverloadData.isEnabled()&&OVERLOAD_DOWN_KEY.consumeClick()) {
                    ClientPlayerOverloadData.subCurrentMultiplier(1);
                    player.displayClientMessage(
                            Component.literal("超载倍数调整为" + ClientPlayerOverloadData.getCurrentMultiplier()).withStyle(ChatFormatting.LIGHT_PURPLE),
                            true
                    );
                    ModMessage.sendToServer(new OverloadDataC2SPacket(ClientPlayerOverloadData.isEnabled(),
                            ClientPlayerOverloadData.getCurrentMultiplier(),ClientPlayerOverloadData.getMaxMultiplier()));
            }
        }
    }

    @Mod.EventBusSubscriber(modid = Spellweaver.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {

        @SubscribeEvent//此处注册按键
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(KeyBinding.TEST_KEY);
            event.register(KeyBinding.SPELL_CAST_KEY1);
            event.register(KeyBinding.SPELL_CAST_KEY2);
            event.register(KeyBinding.SPELL_CAST_KEY3);
            event.register(KeyBinding.SPELL_CAST_KEY4);
            event.register(KeyBinding.SPELL_CAST_KEY5);
            event.register(KeyBinding.SPELL_CAST_KEY6);
            event.register(KeyBinding.SPELL_CAST_KEY7);
            event.register(KeyBinding.SPELL_CAST_KEY8);
            event.register(KeyBinding.SPELL_CAST_KEY9);
            event.register(KeyBinding.SPELL_CAST_KEY10);
            event.register(KeyBinding.OVERLOAD_KEY);
            event.register(KeyBinding.OVERLOAD_UP_KEY);
            event.register(KeyBinding.OVERLOAD_DOWN_KEY);
            Spellweaver.getLOGGER().debug("[Spellweaver:ClientModBusEvents]按键已注册");
        }

        @SubscribeEvent//hud注册
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event ){
            event.registerAboveAll("spellweaver_mana_hud", ManaHud.HUD_MANA);
            Spellweaver.getLOGGER().debug("[Spellweaver:ClientModBusEvents]hud已注册");
        }
        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            //魔法光源实体渲染器
            event.registerEntityRenderer(ModEntities.MAGIC_LIGHT.get(), MagicLightRenderer::new);

            //魔法弹渲染器
            event.registerEntityRenderer(ModEntities.MANA_BALL.get(), PlayerManaBallRenderer::new);

            event.registerEntityRenderer(ModEntities.FROZEN_ICE.get(), FrozenIceRenderer::new);

            event.registerEntityRenderer(ModEntities.MANA_ARROW.get(),
                    (context) -> new ArrowRenderer<ManaArrow>(context) {
                        @Override
                        public ResourceLocation getTextureLocation(ManaArrow entity) {
                            return ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID,"textures/entity/projectiles/mana_arrow.png");
                        }
                    });
            event.registerEntityRenderer(ModEntities.SPELL_EFFECT.get(), SpellEffectRenderer::new);


            event.registerBlockEntityRenderer(
                    ModBlockEntities.SPELL_MACHINE_BLOCK_ENTITY.get(),
                    SpellMachineRenderer::new
            );

        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                Item testBow = ModItems.TEST_BOW.get();
                Item manaBow=ModItems.MANA_BOW.get();
                ResourceLocation PULL =  ResourceLocation.fromNamespaceAndPath("minecraft","pull");
                ResourceLocation PULLING = ResourceLocation.fromNamespaceAndPath("minecraft","pulling");

                ItemProperties.register(testBow, PULL, (stack, world, entity, seed) -> {
                    if (entity == null) return 0.0F;
                    if (entity.getUseItem() != stack) return 0.0F;
                    int useDuration = stack.getUseDuration();
                    int remaining = entity.getUseItemRemainingTicks();
                    return (float) (useDuration - remaining) / 20.0F;
                });
                ItemProperties.register(manaBow, PULLING, (stack, world, entity, seed) -> {
                    return (entity != null && entity.isUsingItem() && entity.getUseItem() == stack) ? 1.0F : 0.0F;
                });

                ItemProperties.register(manaBow, PULL, (stack, world, entity, seed) -> {
                    if (entity == null) return 0.0F;
                    if (entity.getUseItem() != stack) return 0.0F;
                    int useDuration = stack.getUseDuration();
                    int remaining = entity.getUseItemRemainingTicks();
                    return (float) (useDuration - remaining) / 20.0F;
                });
                ItemProperties.register(testBow, PULLING, (stack, world, entity, seed) -> {
                    return (entity != null && entity.isUsingItem() && entity.getUseItem() == stack) ? 1.0F : 0.0F;
                });
            });
        }
    }
}
