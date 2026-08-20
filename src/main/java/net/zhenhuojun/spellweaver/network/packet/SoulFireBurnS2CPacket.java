package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class SoulFireBurnS2CPacket {
    private final double x, y, z;

    public SoulFireBurnS2CPacket(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SoulFireBurnS2CPacket(FriendlyByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            Vec3 center = new Vec3(x, y, z);
            ParticleEngine engine = mc.particleEngine;
            RandomSource random = mc.level.random;

            // 球形扩散的灵魂火粒子
            int flameCount = 16;
            for (int i = 0; i < flameCount; i++) {
                double theta = random.nextDouble() * Math.PI * 2;
                double phi = Math.acos(2 * random.nextDouble() - 1);
                double spd = 0.1 + random.nextDouble() * 0.15;
                double vx = Math.sin(phi) * Math.cos(theta) * spd;
                double vy = Math.sin(phi) * Math.sin(theta) * spd;
                double vz = Math.cos(phi) * spd;

                addParticle(engine, ParticleTypes.SOUL_FIRE_FLAME,
                        center.x, center.y, center.z, vx, vy, vz);
            }

            // 灵魂粒子（向上飘）
            for (int i = 0; i < 8; i++) {
                double ox = (random.nextDouble() - 0.5) * 0.5;
                double oy = random.nextDouble() * 0.5;
                double oz = (random.nextDouble() - 0.5) * 0.5;
                addParticle(engine, ParticleTypes.SOUL,
                        center.x + ox, center.y + oy, center.z + oz,
                        0, 0.02 + random.nextDouble() * 0.03, 0);
            }
        });
        return true;
    }

    @Nullable
    private Particle addParticle(ParticleEngine engine, ParticleOptions options,
                                 double x, double y, double z,
                                 double vx, double vy, double vz) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (camera.getPosition().distanceToSqr(x, y, z) > 1024.0D) return null;
        ParticleStatus status = Minecraft.getInstance().options.particles().get();
        if (status == ParticleStatus.MINIMAL) return null;
        if (status == ParticleStatus.DECREASED && Minecraft.getInstance().level.random.nextInt(3) == 0) return null;
        return engine.createParticle(options, x, y, z, vx, vy, vz);
    }
}
