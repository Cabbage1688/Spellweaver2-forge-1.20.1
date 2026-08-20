package net.zhenhuojun.spellweaver.entity.ai;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.zhenhuojun.spellweaver.Spellweaver;

// 注册魔法之星AI所需的自定义Activity
public class ModActivities {
    public static final DeferredRegister<Activity> ACTIVITY_REGISTER =
            DeferredRegister.create(Registries.ACTIVITY, Spellweaver.MODID);

    public static final RegistryObject<Activity> FOLLOW =
            ACTIVITY_REGISTER.register("magic_star_follow", () -> new Activity("magic_star_follow"));
    public static final RegistryObject<Activity> STOP =
            ACTIVITY_REGISTER.register("magic_star_stop", () -> new Activity("magic_star_stop"));
    public static final RegistryObject<Activity> SLEEP =
            ACTIVITY_REGISTER.register("magic_star_sleep", () -> new Activity("magic_star_sleep"));
    public static final RegistryObject<Activity> PATROL =
            ACTIVITY_REGISTER.register("magic_star_patrol", () -> new Activity("magic_star_patrol"));

    public static void register(IEventBus eventBus) {
        ACTIVITY_REGISTER.register(eventBus);
    }
}
