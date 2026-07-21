package net.zhenhuojun.spellweaver;


import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;


@Mod.EventBusSubscriber(modid = Spellweaver.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();


    private static final ForgeConfigSpec.BooleanValue SHOW_PATTERN_NAME = BUILDER
            .comment("是否在非常量符文图案下方渲染其名称")
            .define("showPatternName", false);

    //闪电
    private static final ForgeConfigSpec.BooleanValue LIGHTNING_USE_LINEAR_MANA = BUILDER
            .comment("是否对闪电法术使用线性法力消耗公式（false = 使用原版指数公式，true = 使用下方线性参数）")
            .define("lightningUseLinearMana", false);
    private static final ForgeConfigSpec.DoubleValue LIGHTNING_LINEAR_MANA_BASE = BUILDER
            .comment("线性公式：基础消耗（仅当 lightningUseLinearMana = true 时生效）")
            .defineInRange("lightningLinearManaBase", 50.0, 0.0,Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue LIGHTNING_LINEAR_MANA_PER_LEVEL = BUILDER
            .comment("线性公式：每等级额外消耗（仅当 lightningUseLinearMana = true 时生效）")
            .defineInRange("lightningLinearManaPerLevel", 10.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue LIGHTNING_DAMAGE_MULTIPLIER = BUILDER
            .comment("闪电法术的伤害倍数，实际伤害 = 倍数 × 攻击等级")
            .defineInRange("lightningDamageMultiplier", 40.0, 0, Double.MAX_VALUE);

    // 音爆
    private static final ForgeConfigSpec.BooleanValue SONIC_USE_LINEAR_MANA = BUILDER
            .comment("是否对音爆法术使用线性法力消耗公式（false = 使用原版指数公式，true = 使用下方线性参数）")
            .define("sonicUseLinearMana", false);
    private static final ForgeConfigSpec.DoubleValue SONIC_LINEAR_MANA_BASE = BUILDER
            .comment("线性公式：基础消耗（仅当 sonicUseLinearMana = true 时生效）")
            .defineInRange("sonicLinearManaBase", 50.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue SONIC_LINEAR_MANA_PER_LEVEL = BUILDER
            .comment("线性公式：每等级额外消耗（仅当 sonicUseLinearMana = true 时生效）")
            .defineInRange("sonicLinearManaPerLevel", 10.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue SONIC_DAMAGE_MULTIPLIER = BUILDER
            .comment("音爆法术的伤害倍数，实际伤害 = 倍数 × 攻击等级")
            .defineInRange("sonicDamageMultiplier", 45.0, 0, Double.MAX_VALUE);

    //元素伤害
    private static final ForgeConfigSpec.BooleanValue ELEMENT_USE_LINEAR_MANA = BUILDER
            .comment("是否对元素攻击使用线性法力消耗公式（false = 使用原版指数公式，true = 使用下方线性参数）")
            .define("elementUseLinearMana", false);
    private static final ForgeConfigSpec.DoubleValue ELEMENT_LINEAR_MANA_BASE = BUILDER
            .comment("线性公式：基础消耗（仅当 elementUseLinearMana = true 时生效）")
            .defineInRange("elementLinearManaBase", 50.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue ELEMENT_LINEAR_MANA_PER_LEVEL = BUILDER
            .comment("线性公式：每等级额外消耗（仅当 elementUseLinearMana = true 时生效）")
            .defineInRange("elementLinearManaPerLevel", 10.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue ELEMENT_DAMAGE_MULTIPLIER = BUILDER
            .comment("元素攻击的全局伤害倍率，实际伤害 = 倍率 × 元素基础系数 × 攻击等级（基础系数：水8，火12，雷10，冰12，风9，末影10）")
            .defineInRange("elementDamageMultiplier", 1.0, 0.0, Double.MAX_VALUE);


    private static final ForgeConfigSpec.DoubleValue BREAK_MANA_MULTIPLIER = BUILDER
            .comment("“破坏”法术的法力消耗倍率，原消耗 = min(硬度², 100)")
            .defineInRange("breakManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue DRIVE_MANA_MULTIPLIER = BUILDER
            .comment("“驱动”法术的法力消耗倍率，原消耗 = |(v+Δv)² - v²|")
            .defineInRange("driveManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue WATER_MANA_MULTIPLIER = BUILDER
            .comment("“水”法术的法力消耗倍率，原消耗 = 15")
            .defineInRange("waterManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue MAGIC_LIGHT_MANA_MULTIPLIER = BUILDER
            .comment("“魔法光源”法术的法力消耗倍率，原消耗 = 5")
            .defineInRange("magicLightManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue TP_MANA_MULTIPLIER = BUILDER
            .comment("“传送”法术的法力消耗倍率，原消耗 = min(3×距离, 600)")
            .defineInRange("tpManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue GROW_MANA_MULTIPLIER = BUILDER
            .comment("“生长”法术的法力消耗倍率，原消耗 = 5×半径²")
            .defineInRange("growManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue HEALTH_MANA_MULTIPLIER = BUILDER
            .comment("“治疗”法术的法力消耗倍率，原消耗 = 5×治疗量")
            .defineInRange("healthManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue SLOW_FALL_MANA_MULTIPLIER = BUILDER
            .comment("“缓降”法术的法力消耗倍率，原消耗 = 4 + 持续时间(秒)")
            .defineInRange("slowFallManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue DIRT_MANA_MULTIPLIER = BUILDER
            .comment("“泥土”法术的法力消耗倍率，原消耗 = 10")
            .defineInRange("dirtManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue SNOW_MANA_MULTIPLIER = BUILDER
            .comment("“细雪”法术的法力消耗倍率，原消耗 = 20")
            .defineInRange("snowManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue SAND_MANA_MULTIPLIER = BUILDER
            .comment("“沙”法术的法力消耗倍率，原消耗 = 10")
            .defineInRange("sandManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue LAVA_MANA_MULTIPLIER = BUILDER
            .comment("“岩浆”法术的法力消耗倍率，原消耗 = 10")
            .defineInRange("lavaManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue MANA_BALL_MANA_MULTIPLIER = BUILDER
            .comment("“魔法飞弹”法术的法力消耗倍率，原消耗 = 符文数量")
            .defineInRange("manaBallManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue MANA_SWORD_MANA_MULTIPLIER = BUILDER
            .comment("“幻化之剑”法术的法力消耗倍率，原消耗 = 25")
            .defineInRange("manaSwordManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue MANA_BOW_MANA_MULTIPLIER = BUILDER
            .comment("“幻化之弓”法术的法力消耗倍率，原消耗 = 25")
            .defineInRange("manaBowManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue BOOM_MANA_MULTIPLIER = BUILDER
            .comment("“爆炸”法术的法力消耗倍率，原消耗 = 10×半径¹·⁵")
            .defineInRange("boomManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue BLOCK_RAY_MANA_MULTIPLIER = BUILDER
            .comment("“方块射线”法术的法力消耗倍率，原消耗 = 0.4105×(e^(0.16×距离)-1)")
            .defineInRange("blockRayManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue ENTITY_RAY_MANA_MULTIPLIER = BUILDER
            .comment("“实体射线”法术的法力消耗倍率，原消耗 = 0.4105×(e^(0.16×距离)-1)")
            .defineInRange("entityRayManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue IGNITE_MANA_MULTIPLIER = BUILDER
            .comment("“点燃”法术的法力消耗倍率，原消耗 = 5")
            .defineInRange("igniteManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue PURIFY_MANA_MULTIPLIER = BUILDER
            .comment("“净化”法术的法力消耗倍率，原消耗 = 20×消除的效果数量")
            .defineInRange("purifyManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue REPAIR_MANA_MULTIPLIER = BUILDER
            .comment("“修复”法术的法力消耗倍率，原消耗 = |修复值|")
            .defineInRange("repairManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue PLACE_MANA_MULTIPLIER = BUILDER
            .comment("“方块放置”法术的法力消耗倍率，原消耗 = 2")
            .defineInRange("placeManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue TRANSFER_MANA_MULTIPLIER = BUILDER
            .comment("“物品转移”法术的法力消耗倍率，原消耗 = 源物品数量")
            .defineInRange("transferManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue SATURATE_MANA_MULTIPLIER = BUILDER
            .comment("“饱腹”法术的法力消耗倍率，原消耗 = 2×(20-饥饿值) + 5×(10-饱和度)")
            .defineInRange("saturateManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue MANA_SHIELD_MANA_MULTIPLIER = BUILDER
            .comment("“魔法护盾”法术的法力消耗倍率（消耗法力 = 护盾量 × 倍率）")
            .defineInRange("manaShieldManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue INTERACT_MANA_MULTIPLIER = BUILDER
            .comment("“交互”法术的法力消耗倍率，原消耗 = 1")
            .defineInRange("interactManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue CULTIVATE_MANA_MULTIPLIER = BUILDER
            .comment("“播种”法术的法力消耗倍率，原消耗 = 1")
            .defineInRange("cultivateManaMultiplier", 1.0, 0.0, Double.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue MANA_BAR_POSITION=BUILDER
            .comment("魔力条的位置，0，1，2，3分别代表右下，左下，左上，右上")
            .defineInRange("manaBarPosition",0,0,Integer.MAX_VALUE);



    public static boolean showPatternName;

    public static boolean lightningUseLinearMana;
    public static double lightningLinearManaBase;
    public static double lightningLinearManaPerLevel;
    public static double lightningDamageMultiplier;
    public static boolean sonicUseLinearMana;
    public static double sonicLinearManaBase;
    public static double sonicLinearManaPerLevel;
    public static double sonicDamageMultiplier;

    public static boolean elementUseLinearMana;
    public static double elementLinearManaBase;
    public static double elementLinearManaPerLevel;
    public static double elementDamageMultiplier;

    public static double breakManaMultiplier;
    public static double driveManaMultiplier;
    public static double waterManaMultiplier;
    public static double magicLightManaMultiplier;
    public static double tpManaMultiplier;
    public static double growManaMultiplier;
    public static double healthManaMultiplier;
    public static double slowFallManaMultiplier;
    public static double dirtManaMultiplier;
    public static double snowManaMultiplier;
    public static double sandManaMultiplier;
    public static double lavaManaMultiplier;
    public static double manaBallManaMultiplier;
    public static double manaSwordManaMultiplier;
    public static double manaBowManaMultiplier;
    public static double boomManaMultiplier;
    public static double blockRayManaMultiplier;
    public static double entityRayManaMultiplier;
    public static double igniteManaMultiplier;
    public static double purifyManaMultiplier;
    public static double repairManaMultiplier;
    public static double placeManaMultiplier;
    public static double transferManaMultiplier;
    public static double saturateManaMultiplier;
    public static double manaShieldManaMultiplier;
    public static double interactManaMultiplier;
    public static double cultivateManaMultiplier;

    public static int manaBarPosition;

    static final ForgeConfigSpec SPEC = BUILDER.build();
    ///Forge 的 ModConfigEvent 分为 Loading（游戏启动或进入世界时触发）
    ///和 Reloading（通过 /forge config reload 命令热重载时触发）两个子事件。
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        //showPatternName = SHOW_PATTERN_NAME.get();
        reloadConfig();
    }

    @SubscribeEvent
    static void onReload(final ModConfigEvent.Reloading event)
    {
        reloadConfig();
    }

    private static void reloadConfig()
    {
        showPatternName = SHOW_PATTERN_NAME.get();
        lightningUseLinearMana = LIGHTNING_USE_LINEAR_MANA.get();
        lightningLinearManaBase = LIGHTNING_LINEAR_MANA_BASE.get();
        lightningLinearManaPerLevel = LIGHTNING_LINEAR_MANA_PER_LEVEL.get();
        lightningDamageMultiplier = LIGHTNING_DAMAGE_MULTIPLIER.get();
        sonicUseLinearMana = SONIC_USE_LINEAR_MANA.get();
        sonicLinearManaBase = SONIC_LINEAR_MANA_BASE.get();
        sonicLinearManaPerLevel = SONIC_LINEAR_MANA_PER_LEVEL.get();
        sonicDamageMultiplier = SONIC_DAMAGE_MULTIPLIER.get();
        elementUseLinearMana = ELEMENT_USE_LINEAR_MANA.get();
        elementLinearManaBase = ELEMENT_LINEAR_MANA_BASE.get();
        elementLinearManaPerLevel = ELEMENT_LINEAR_MANA_PER_LEVEL.get();
        elementDamageMultiplier = ELEMENT_DAMAGE_MULTIPLIER.get();
        breakManaMultiplier = BREAK_MANA_MULTIPLIER.get();
        driveManaMultiplier = DRIVE_MANA_MULTIPLIER.get();
        waterManaMultiplier = WATER_MANA_MULTIPLIER.get();
        magicLightManaMultiplier = MAGIC_LIGHT_MANA_MULTIPLIER.get();
        tpManaMultiplier = TP_MANA_MULTIPLIER.get();
        growManaMultiplier = GROW_MANA_MULTIPLIER.get();
        healthManaMultiplier = HEALTH_MANA_MULTIPLIER.get();
        slowFallManaMultiplier = SLOW_FALL_MANA_MULTIPLIER.get();
        dirtManaMultiplier = DIRT_MANA_MULTIPLIER.get();
        snowManaMultiplier = SNOW_MANA_MULTIPLIER.get();
        sandManaMultiplier = SAND_MANA_MULTIPLIER.get();
        lavaManaMultiplier = LAVA_MANA_MULTIPLIER.get();
        manaBallManaMultiplier = MANA_BALL_MANA_MULTIPLIER.get();
        manaSwordManaMultiplier = MANA_SWORD_MANA_MULTIPLIER.get();
        manaBowManaMultiplier = MANA_BOW_MANA_MULTIPLIER.get();
        boomManaMultiplier = BOOM_MANA_MULTIPLIER.get();
        blockRayManaMultiplier = BLOCK_RAY_MANA_MULTIPLIER.get();
        entityRayManaMultiplier = ENTITY_RAY_MANA_MULTIPLIER.get();
        igniteManaMultiplier = IGNITE_MANA_MULTIPLIER.get();
        purifyManaMultiplier = PURIFY_MANA_MULTIPLIER.get();
        repairManaMultiplier = REPAIR_MANA_MULTIPLIER.get();
        placeManaMultiplier = PLACE_MANA_MULTIPLIER.get();
        transferManaMultiplier = TRANSFER_MANA_MULTIPLIER.get();
        saturateManaMultiplier = SATURATE_MANA_MULTIPLIER.get();
        manaShieldManaMultiplier = MANA_SHIELD_MANA_MULTIPLIER.get();
        interactManaMultiplier = INTERACT_MANA_MULTIPLIER.get();
        cultivateManaMultiplier = CULTIVATE_MANA_MULTIPLIER.get();
        manaBarPosition=MANA_BAR_POSITION.get();
    }
}


