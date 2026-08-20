package net.zhenhuojun.spellweaver.entity.ai;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.entity.ai.sensing.MagicStarTargetSensor;

// 注册魔法之星AI所需的自定义Sensor
public class ModSensors {
    public static final DeferredRegister<SensorType<?>> SENSOR_REGISTER =
            DeferredRegister.create(Registries.SENSOR_TYPE, Spellweaver.MODID);

    public static final RegistryObject<SensorType<MagicStarTargetSensor>> MAGIC_STAR_TARGET =
            SENSOR_REGISTER.register("magic_star_target", () -> new SensorType<>(MagicStarTargetSensor::new));

    public static void register(IEventBus eventBus) {
        SENSOR_REGISTER.register(eventBus);
    }
}
