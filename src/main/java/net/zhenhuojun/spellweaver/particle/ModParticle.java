package net.zhenhuojun.spellweaver.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.zhenhuojun.spellweaver.Spellweaver;

public class ModParticle {


    public static final DeferredRegister<ParticleType<?>> PARTICLE_DEFERRED_REGISTER =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Spellweaver.MODID);

    public static void register(IEventBus eventBus) {
        PARTICLE_DEFERRED_REGISTER.register(eventBus);
    }

    //这个纯纯给拖尾方法占位用的
    public static final RegistryObject<SimpleParticleType> BLANK_PARTICLE =
            PARTICLE_DEFERRED_REGISTER.register("blank_particle",
                    () -> new SimpleParticleType(true));

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpecial(ModParticle.BLANK_PARTICLE.get(),
                (type, level, x, y, z, xSpeed, ySpeed, zSpeed) -> null);
    }
}
