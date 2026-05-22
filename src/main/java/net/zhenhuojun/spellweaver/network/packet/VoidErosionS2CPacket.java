package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
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

public class VoidErosionS2CPacket {
    private final double x, y, z;

    // 服务端构造（传入爆发中心位置）
    public VoidErosionS2CPacket(Vec3 pos) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
    }

    public VoidErosionS2CPacket(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // 从字节流反序列化
    public VoidErosionS2CPacket(FriendlyByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
    }

    // 序列化
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
    }

    // 客户端处理
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            Vec3 center = new Vec3(x, y, z);
            ClientLevel level = mc.level;
            ParticleEngine engine = mc.particleEngine;
            RandomSource random = level.random;



            // 螺旋扩散
            int erosionColor = 0x5E2D7E; // 深紫色
            float r = ((erosionColor >> 16) & 0xFF) / 255.0F;
            float g = ((erosionColor >> 8) & 0xFF) / 255.0F;
            float b = (erosionColor & 0xFF) / 255.0F;
            for (int i = 0; i < 150; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double radius = random.nextDouble() * 2.5;
                double dx = Math.cos(angle) * radius;
                double dz = Math.sin(angle) * radius;
                double dy = random.nextDouble() * 1.5;
                // 速度向外螺旋上升
                double speedX = dx * 0.15;
                double speedY = 0.02 + random.nextDouble() * 0.1;
                double speedZ = dz * 0.15;

                Particle particle = addParticle(engine, ParticleTypes.PORTAL,
                        center.x + dx * 0.1, center.y + 0.5 + dy * 0.1, center.z + dz * 0.1,
                        speedX, speedY, speedZ);
                if (particle != null) {
                    float f = 0.6F + random.nextFloat() * 0.4F;
                    particle.setColor(r * f, g * f, b * f);
                    particle.setLifetime(10+10 + random.nextInt(10)); // 短暂存在，更像侵蚀
                }
            }

            //向中心收缩的粒子
            for (int i = 0; i < 80; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double radius = 1.0 + random.nextDouble() * 2.0;
                double dx = Math.cos(angle) * radius;
                double dz = Math.sin(angle) * radius;
                double dy = random.nextDouble() * 2.0;
                // 速度指向中心略向上
                double speedX = -dx * 0.1;
                double speedY = -dy * 0.1 + 0.02;
                double speedZ = -dz * 0.1;

                Particle particle = addParticle(engine, ParticleTypes.PORTAL,//EFFECT,
                        center.x + dx, center.y + dy, center.z + dz,
                        speedX, speedY, speedZ);
                if (particle != null) {
                    particle.setColor(r, g, b);
                    particle.setLifetime(8+8 + random.nextInt(6));
                }
            }


        });
        return true;
    }

    /**
     * 带距离判断和粒子选项的生成方法，照搬 ManaBallEffectS2CPacket 的实现
     */
    @Nullable
    private Particle addParticle(ParticleEngine engine, ParticleOptions options,
                                 double x, double y, double z,
                                 double vx, double vy, double vz) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (camera.getPosition().distanceToSqr(x, y, z) > 1024.0D) {
            return null;
        }
        ParticleStatus status = Minecraft.getInstance().options.particles().get();
        if (status == ParticleStatus.MINIMAL) {
            return null;
        }
        if (status == ParticleStatus.DECREASED && Minecraft.getInstance().level.random.nextInt(3) == 0) {
            return null;
        }
        return engine.createParticle(options, x, y, z, vx, vy, vz);
    }

}