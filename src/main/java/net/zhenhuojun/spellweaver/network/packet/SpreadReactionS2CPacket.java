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

public class SpreadReactionS2CPacket {
    private final double x, y, z;
    private final int color;          // 粒子颜色 RGB
    private final int particleCount;  // 粒子数量
    private final float speed;        // 扩散速度倍率

    // 服务端直接构造，颜色根据元素预定义
    public SpreadReactionS2CPacket(Vec3 pos, int color) {
        this(pos.x, pos.y + 0.5, pos.z, color, 120*2, 1.0f);
    }

    public SpreadReactionS2CPacket(double x, double y, double z, int color, int particleCount, float speed) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
        this.particleCount = particleCount;
        this.speed = speed;
    }

    // 反序列化
    public SpreadReactionS2CPacket(FriendlyByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.color = buf.readInt();
        this.particleCount = buf.readInt();
        this.speed = buf.readFloat();
    }

    // 序列化
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeInt(color);
        buf.writeInt(particleCount);
        buf.writeFloat(speed);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            Vec3 center = new Vec3(x, y, z);
            ParticleEngine engine = mc.particleEngine;
            RandomSource random = mc.level.random;

            // 颜色分量
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;

            for (int i = 0; i < particleCount; i++) {
                // 随机方向，球形均匀分布
                double theta = random.nextDouble() * Math.PI * 2;
                double phi = Math.acos(2 * random.nextDouble() - 1);
                double baseRadius = 0.4 + random.nextDouble() * 0.6; // 起始半径较小
                double dx = Math.sin(phi) * Math.cos(theta) * baseRadius;
                double dy = Math.sin(phi) * Math.sin(theta) * baseRadius;
                double dz = Math.cos(phi) * baseRadius;

                // 速度沿径向向外，速度大小由 speed 控制
                double spd = (0.15 + random.nextDouble() * 0.3) * speed;
                double vx = Math.sin(phi) * Math.cos(theta) * spd;
                double vy = Math.sin(phi) * Math.sin(theta) * spd;
                double vz = Math.cos(phi) * spd;

                Particle particle = addParticle(engine, ParticleTypes.CLOUD,
                        center.x + dx, center.y + dy, center.z + dz,
                        vx, vy, vz);
                if (particle != null) {
                    float f = 0.7f + random.nextFloat() * 0.3f;
                    particle.setColor(r * f, g * f, b * f);
                    particle.setLifetime(8 + random.nextInt(8)); // 短生命周期，扩散感强
                }
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