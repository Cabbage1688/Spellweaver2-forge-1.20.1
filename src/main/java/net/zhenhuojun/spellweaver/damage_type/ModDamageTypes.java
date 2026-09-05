package net.zhenhuojun.spellweaver.damage_type;


import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.zhenhuojun.spellweaver.Spellweaver;

public class ModDamageTypes {

    public static final ResourceKey<DamageType> ELEMENT_FIRE =
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID, "element_fire"));

    public static final ResourceKey<DamageType> ELEMENT_WATER =
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID, "element_water"));

    public static final ResourceKey<DamageType> ELEMENT_ENDER =
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID, "element_ender"));

    public static final ResourceKey<DamageType> ELEMENT_ICE =
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID, "element_ice"));

    public static final ResourceKey<DamageType> ELEMENT_LIGHTNING =
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID, "element_lightning"));

    public static final ResourceKey<DamageType> ELEMENT_WIND=
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID, "element_wind"));

    public static final ResourceKey<DamageType> MANA_SLASH=
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID, "mana_slash"));
}
