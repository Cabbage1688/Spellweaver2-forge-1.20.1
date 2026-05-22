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

public class RayS2CPacket {
    private final double x1, y1, z1; // 射线起点
    private final double x2, y2, z2; // 射线终点
    private final int color;         // 粒子颜色 RGB
    private final int particleCount; // 总粒子数（<=0 时自动按距离计算）
    private final float speed;       // 粒子运动速度倍率

    /**
     * 简易构造：起点、终点、颜色，粒子数量自动按距离生成
     */
    public RayS2CPacket(Vec3 start, Vec3 end, int color) {
        this(start.x, start.y, start.z, end.x, end.y, end.z, color, -1, 0.02f);
    }

    public RayS2CPacket(double x1, double y1, double z1, double x2, double y2, double z2,
                        int color, int particleCount, float speed) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        this.color = color;
        this.particleCount = particleCount;
        this.speed = speed;
    }

    // 反序列化
    public RayS2CPacket(FriendlyByteBuf buf) {
        this.x1 = buf.readDouble();
        this.y1 = buf.readDouble();
        this.z1 = buf.readDouble();
        this.x2 = buf.readDouble();
        this.y2 = buf.readDouble();
        this.z2 = buf.readDouble();
        this.color = buf.readInt();
        this.particleCount = buf.readInt();
        this.speed = buf.readFloat();
    }

    // 序列化
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(x1);
        buf.writeDouble(y1);
        buf.writeDouble(z1);
        buf.writeDouble(x2);
        buf.writeDouble(y2);
        buf.writeDouble(z2);
        buf.writeInt(color);
        buf.writeInt(particleCount);
        buf.writeFloat(speed);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            Vec3 start = new Vec3(x1, y1, z1);
            Vec3 end = new Vec3(x2, y2, z2);
            Vec3 dir = end.subtract(start);
            double length = dir.length();
            if (length < 0.001) return;

            Vec3 step = dir.normalize();

            // 计算实际粒子数量
            int count = particleCount > 0 ? particleCount : (int) (length * 10); // 默认每块10个粒子
            if (count < 20) count = 20; // 保证最少有20个

            ParticleEngine engine = mc.particleEngine;
            RandomSource random = mc.level.random;

            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;

            for (int i = 0; i < count; i++) {
                // 沿射线均匀分布，加入少量随机偏移避免死板
                double t = (i + random.nextDouble() * 0.3 - 0.15) / count;
                t = Math.max(0, Math.min(1, t));
                double px = start.x + dir.x * t + (random.nextDouble() - 0.5) * 0.05;
                double py = start.y + dir.y * t + (random.nextDouble() - 0.5) * 0.05;
                double pz = start.z + dir.z * t + (random.nextDouble() - 0.5) * 0.05;

                //沿射线方向或随机扩散
                double vx = (random.nextDouble() - 0.5) * 0.1 * speed;
                double vy = (random.nextDouble() - 0.5) * 0.1 * speed;
                double vz = (random.nextDouble() - 0.5) * 0.1 * speed;

                // 创建粒子
                Particle particle = addParticle(engine, ParticleTypes.CLOUD, px, py, pz, vx, vy, vz);
                if (particle != null) {
                    float brightness = 0.6f + random.nextFloat() * 0.4f;
                    particle.setColor(r * brightness, g * brightness, b * brightness);
                    particle.setLifetime(5 + random.nextInt(10)); // 短生命周期，形成持续射线感
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