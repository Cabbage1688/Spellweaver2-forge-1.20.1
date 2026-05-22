package net.zhenhuojun.spellweaver.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.capability.impl.long_term_variables.PlayerLongTermVariablesData;
import net.zhenhuojun.spellweaver.capability.impl.mana.PlayerMana;
import net.zhenhuojun.spellweaver.capability.impl.overload.PlayerManaOverload;
import net.zhenhuojun.spellweaver.capability.impl.scroll.ScrollSpell;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.PlayerSpellStorage;
import net.zhenhuojun.spellweaver.capability.provider.mana.*;

@Mod.EventBusSubscriber(modid = Spellweaver.MODID)
public class CapabilityHandler {//这个类管理能力相关的东西
    public static final ResourceLocation MANA_CAPABILITY = ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID, "mana");
    public static final ResourceLocation SPELL_STORAGE_CAPABILITY= ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID,"spell_storage");
    public static final ResourceLocation VARIABLE_CAPABILITY=ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID,"variable");
    public static final ResourceLocation OVERLOAD_CAPABILITY=ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID,"overload");
    public static final ResourceLocation SCROLL_SPELL_HANDLER=ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID,"scroll_spell_handler");

    @SubscribeEvent//能力附加于实体
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            //Spellweaver.getLOGGER().debug("[CapAttach] 为玩家附加能力: {}", player);
            //event.addCapability(MANA_CAPABILITY, new PlayerManaProvider(player));
            //event.addCapability(SPELL_STORAGE_CAPABILITY,new PlayerSpellStorageProvider(player));

            if(!event.getObject().getCapability(PlayerManaProvider.PLAYER_MANA).isPresent()) {
                event.addCapability(MANA_CAPABILITY, new PlayerManaProvider(player));
                Spellweaver.getLOGGER().debug("[CapAttach] 魔力能力已经附加能力");
            }

            if(!event.getObject().getCapability(PlayerSpellStorageProvider.PLAYER_SPELL_STORAGE).isPresent()) {
                event.addCapability(SPELL_STORAGE_CAPABILITY, new PlayerSpellStorageProvider(player));
            }

            if(!event.getObject().getCapability(PlayerLongTermVariablesProvider.PLAYER_LONG_TERM_VARIABLES).isPresent()){
                event.addCapability(VARIABLE_CAPABILITY,new PlayerLongTermVariablesProvider());
            }
            if(!event.getObject().getCapability(PlayerManaOverloadProvider.PLAYER_MANA_OVERLOAD).isPresent()){
                event.addCapability(OVERLOAD_CAPABILITY,new PlayerManaOverloadProvider());
                Spellweaver.getLOGGER().debug("[CapAttach] 超载能力已经附加能力");
            }

        }
    }
    @SubscribeEvent
    public static void onItemAttachCapabilities(AttachCapabilitiesEvent<ItemStack> event){
        //条件判断一删就能跑了，神了。
        //if(!event.getObject().getCapability(ScrollSpellProvider.SCROLL_SPELL_CAPABILITY).isPresent()){
            event.addCapability(SCROLL_SPELL_HANDLER,new ScrollSpellProvider());
       // }
    }
    @SubscribeEvent//能力注册,这个不要写成static，文档里就是这么写的
    public void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerMana.class);
        event.register(PlayerSpellStorage.class);
        event.register(PlayerLongTermVariablesData.class);
        event.register(PlayerManaOverload.class);
        event.register(ScrollSpell.class);
    }

}
