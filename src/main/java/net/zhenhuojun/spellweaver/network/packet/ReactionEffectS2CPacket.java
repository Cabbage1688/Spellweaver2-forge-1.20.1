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

public class ReactionEffectS2CPacket {
    public enum ReactionType {
        ELECTROCUTE,   // 感电
        CONDUCT,       // 传导
        COMBUST,       // 助燃
        CHILL          // 风寒
    }

    private final double x, y, z;
    private final ReactionType type;

    // 服务端构造
    public ReactionEffectS2CPacket(Vec3 pos, ReactionType type) {
        this(pos.x, pos.y + 0.5, pos.z, type);
    }

    public ReactionEffectS2CPacket(double x, double y, double z, ReactionType type) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
    }

    // 反序列化
    public ReactionEffectS2CPacket(FriendlyByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.type = buf.readEnum(ReactionType.class);
    }

    // 序列化
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeEnum(type);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            Vec3 center = new Vec3(x, y, z);
            ParticleEngine engine = mc.particleEngine;
            RandomSource random = mc.level.random;

            switch (type) {
                case ELECTROCUTE -> spawnElectrocute(center, engine, random);
                case CONDUCT -> spawnConduct(center, engine, random);
                case COMBUST -> spawnCombust(center, engine, random);
                case CHILL -> spawnChill(center, engine, random);
            }
        });
        return true;
    }

    //这个就这样了，不再改了
    private void spawnElectrocute(Vec3 center, ParticleEngine engine, RandomSource random) {
        // 闪光粒子：亮白/电光蓝，快速不规则爆散，生命周期极短
        int flashCount = 80;
        for (int i = 0; i < flashCount; i++) {
            // 方向略微偏水平，更尖锐
            double angle = random.nextDouble() * Math.PI * 2;
            double pitch = (random.nextDouble() - 0.5) * Math.PI * 0.6; // 限制竖直扩散
            double spd = 0.2 + random.nextDouble() * 0.4;
            double vx = Math.cos(pitch) * Math.cos(angle) * spd*2;
            double vy = Math.sin(pitch) * spd*2;
            double vz = Math.cos(pitch) * Math.sin(angle) * spd*2;

            // 起始位置靠近中心
            double px = center.x + vx * 0.1;
            double py = center.y + vy * 0.1;
            double pz = center.z + vz * 0.1;

            // 颜色：随机在亮白和电光蓝之间
            boolean white = random.nextBoolean();
            int color = white ? 0xFFFFFF : 0x00BFFF;
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;

            Particle p = addParticle(engine, ParticleTypes.ELECTRIC_SPARK, px, py, pz, vx, vy, vz);
            if (p != null) {
                p.setColor(r, g, b);
                p.setLifetime(6+7 + 3*random.nextInt(5));
            }
        }
        // 烟雾粒子缓慢升起扩散，模拟焦烟
        int smokeCount = 18*2;
        for (int i = 0; i < smokeCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double horizSpd = 0.01 + random.nextDouble() * 0.03;
            double vx = Math.cos(angle) * horizSpd;
            double vy = 0.02 + random.nextDouble() * 0.04;
            double vz = Math.sin(angle) * horizSpd;
            Particle p = addParticle(engine, ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    center.x, center.y, center.z, vx, vy, vz);
            if (p != null) {
                p.setLifetime(30 + random.nextInt(15));
            }
        }
    }

    private void spawnConduct(Vec3 center, ParticleEngine engine, RandomSource random) {


        for (int i = 0; i < 80; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = 0.2 + random.nextDouble() * 0.6;
            double dx = Math.cos(angle) * radius;
            double dz = Math.sin(angle) * radius;
            double vy = 0.25 + random.nextDouble() * 0.3*2*2;
            // 水平速度向外螺旋
            double vx = dx * 0.1*2;
            double vz = dz * 0.1*2;

            int color = 0xFFFFFF;
            float r = 1.0f, g = 1.0f, b = 1.0f;
            Particle p = addParticle(engine, ParticleTypes.ELECTRIC_SPARK,
                    center.x + dx, center.y, center.z + dz, vx, vy, vz);
            if (p != null) {
                p.setColor(r, g, b);
                p.setLifetime(5+5 + random.nextInt(6));
            }
        }


    }

    private void spawnCombust(Vec3 center, ParticleEngine engine, RandomSource random) {
        // 1. 急剧膨胀的橙色爆发
        int expandCount = 100;
        for (int i = 0; i < expandCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double pitch = Math.acos(2 * random.nextDouble() - 1);
            double spd = 0.25 + random.nextDouble() * 0.35;
            double vx = Math.sin(pitch) * Math.cos(angle) * spd;
            double vy = Math.sin(pitch) * Math.sin(angle) * spd;
            double vz = Math.cos(pitch) * spd;

            int color = 0xFF6A00;
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;
            Particle p = addParticle(engine, ParticleTypes.FLAME,
                    center.x, center.y, center.z, vx, vy, vz);
            if (p != null) {
                p.setColor(r, g, b);
                p.setLifetime(10 + random.nextInt(8));
            }
        }

        // 2. 余烬螺旋飞出（红色粒子）
        int emberCount = 50;
        for (int i = 0; i < emberCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = 0.1 + random.nextDouble() * 0.5;
            double dx = Math.cos(angle) * radius;
            double dz = Math.sin(angle) * radius;
            double vy = 0.05 + random.nextDouble() * 0.15;
            double vx = dx * 0.2;
            double vz = dz * 0.2;

            int color = 0xFF4500; // 炽红
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;
            Particle p = addParticle(engine, ParticleTypes.INSTANT_EFFECT,
                    center.x + dx, center.y, center.z + dz, vx, vy, vz);
            if (p != null) {
                p.setColor(r, g, b);
                p.setLifetime(8 + random.nextInt(6));
            }
        }

        // 3. 少量火焰粒子贴在中心周围
        for (int i = 0; i < 30; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = 0.2 + random.nextDouble() * 0.8;
            double dx = Math.cos(angle) * radius;
            double dz = Math.sin(angle) * radius;
            double dy = random.nextDouble() * 1.5;
            Particle p = addParticle(engine, ParticleTypes.FLAME,
                    center.x + dx, center.y + dy, center.z + dz, 0, 0, 0);
            if (p != null) {
                p.setLifetime(10 + random.nextInt(5));
            }
        }
    }

    private void spawnChill(Vec3 center, ParticleEngine engine, RandomSource random) {
        // 1. 白色冰晶水平旋转扩散
        int whiteCount = 120;
        for (int i = 0; i < whiteCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double pitch = (random.nextDouble() - 0.5) * Math.PI * 0.4; // 偏向水平
            double spd = 0.15 + random.nextDouble() * 0.3;
            // 速度主要水平
            double vx = Math.cos(pitch) * Math.cos(angle) * spd;
            double vy = Math.sin(pitch) * spd * 0.3; // 垂直速度较小
            double vz = Math.cos(pitch) * Math.sin(angle) * spd;

            Particle p = addParticle(engine, ParticleTypes.SNOWFLAKE,
                    center.x, center.y, center.z, vx, vy, vz);
            if (p != null) {
                p.setColor(1.0f, 1.0f, 1.0f); // 白色
                p.setLifetime(8 + random.nextInt(7));
            }
        }

        // 2. 被风吹的横飞雪花
        int snowCount = 40;
        for (int i = 0; i < snowCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double spd = 0.1 + random.nextDouble() * 0.3;
            double vx = Math.cos(angle) * spd;
            double vz = Math.sin(angle) * spd;
            double vy = 0.01 + random.nextDouble() * 0.04; // 微微向上
            Particle p = addParticle(engine, ParticleTypes.SNOWFLAKE,
                    center.x, center.y, center.z, vx, vy, vz);
            if (p != null) {
                p.setLifetime(20 + random.nextInt(15));
            }
        }

        // 3. 少量淡冰蓝点缀
        int blueCount = 30;
        for (int i = 0; i < blueCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double spd = 0.1 + random.nextDouble() * 0.2;
            double vx = Math.cos(angle) * spd;
            double vy = 0.02 + random.nextDouble() * 0.05;
            double vz = Math.sin(angle) * spd;
            int color = 0xC8F0FF;
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;
            Particle p = addParticle(engine, ParticleTypes.SNOWFLAKE,
                    center.x, center.y, center.z, vx, vy, vz);
            if (p != null) {
                p.setColor(r, g, b);
                p.setLifetime(10 + random.nextInt(6));
            }
        }
    }

    // ---------- 辅助方法 ----------
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