package net.zhenhuojun.spellweaver.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.entity.impl.*;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Spellweaver.MODID,bus=Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {
    //注册器
    public static final DeferredRegister<EntityType<?>> ENTITY_DEFERRED_REGISTER= DeferredRegister
            .create(Registries.ENTITY_TYPE, Spellweaver.MODID);

    public static void register(IEventBus eventBus){
        ENTITY_DEFERRED_REGISTER.register(eventBus);
    }

    //注册魔法光源实体
    public static final Supplier<EntityType<MagicLightEntity>> MAGIC_LIGHT =
            ENTITY_DEFERRED_REGISTER.register("magic_light",() -> EntityType.Builder.of(MagicLightEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(10)
                    .updateInterval(20)
                    .build("magic_light"));

    //魔法球
    public static final Supplier<EntityType<ManaBall>> MANA_BALL=
            ENTITY_DEFERRED_REGISTER.register("player_mana_ball",()->EntityType.Builder.<ManaBall>of(ManaBall::new, MobCategory.MISC)
                    .sized(1.0f,1.0f)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .build("player_mana_ball"));

    public static final Supplier<EntityType<FrozenIceEntity>> FROZEN_ICE=
            ENTITY_DEFERRED_REGISTER.register("frozen_ice",()->EntityType.Builder.<FrozenIceEntity>of(FrozenIceEntity::new,MobCategory.MISC)
                    .sized(1.0f,1.0f)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .build("frozen_ice"));

    public static final RegistryObject<EntityType<ManaArrow>> MANA_ARROW = ENTITY_DEFERRED_REGISTER.register("mana_arrow",
            () -> EntityType.Builder.<ManaArrow>of(ManaArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .build("mana_arrow"));

    public static final RegistryObject<EntityType<SpellEffectEntity>> SPELL_EFFECT =
            ENTITY_DEFERRED_REGISTER.register("spell_effect_entity",
                    () -> EntityType.Builder.<SpellEffectEntity>of(SpellEffectEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(10)
                            .updateInterval(2)
                            .build("spell_effect_entity")
            );


}
