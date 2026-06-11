package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PurifyEffectS2CPacket {

    private final double x, y, z;

    public PurifyEffectS2CPacket(Vec3 pos) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
    }

    public PurifyEffectS2CPacket(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public PurifyEffectS2CPacket(FriendlyByteBuf buf) {
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
            Vec3 center = new Vec3(x, y, z);
            RandomSource random = Minecraft.getInstance().level != null ?
                    Minecraft.getInstance().level.random : RandomSource.create();

            int rings = 3;               // 环的层数
            double startRadius = 0.5;    // 起始半径
            double radiusStep = 0.6;     // 半径递增步长
            int particlesPerRing = 24;    // 每层环的粒子数（最内层）
            float outwardSpeed = 0.35f;   // 向外扩散的速度基数
            float upwardSpeed = 0.15f;    // 向上飘浮的速度基数

            for (int ring = 0; ring < rings; ring++) {
                double radius = startRadius + ring * radiusStep;
                int count = particlesPerRing + ring * 8; // 外层粒子稍多

                for (int i = 0; i < count; i++) {
                    double angle = 2 * Math.PI * i / count;
                    double angleOffset = (random.nextDouble() - 0.5) * 0.3;
                    angle += angleOffset;

                    double dx = Math.cos(angle) * radius;
                    double dz = Math.sin(angle) * radius;
                    double px = center.x + dx;
                    double pz = center.z + dz;
                    double py = center.y + 0.2 + random.nextDouble() * 0.5;

                    double vx = dx * outwardSpeed + (random.nextDouble() - 0.5) * 0.1;
                    double vz = dz * outwardSpeed + (random.nextDouble() - 0.5) * 0.1;
                    double vy = upwardSpeed + random.nextDouble() * 0.2;

                    Minecraft.getInstance().particleEngine.createParticle(
                            ParticleTypes.HAPPY_VILLAGER,
                            px, py, pz,
                            vx, vy, vz
                    );
                }
            }

            // 额外在中心增加一圈向上飘散的小粒子增强净化感
            for (int i = 0; i < 40; i++) {
                double angle = random.nextDouble() * 2 * Math.PI;
                double radius = 0.3;
                double dx = Math.cos(angle) * radius;
                double dz = Math.sin(angle) * radius;
                double px = center.x + dx;
                double pz = center.z + dz;
                double py = center.y + 0.1;
                double vx = (random.nextDouble() - 0.5) * 0.2;
                double vz = (random.nextDouble() - 0.5) * 0.2;
                double vy = random.nextDouble() * 0.4 + 0.1;
                Minecraft.getInstance().particleEngine.createParticle(
                        ParticleTypes.HAPPY_VILLAGER,
                        px, py, pz,
                        vx, vy, vz
                );
            }
        });
        return true;
    }
}
