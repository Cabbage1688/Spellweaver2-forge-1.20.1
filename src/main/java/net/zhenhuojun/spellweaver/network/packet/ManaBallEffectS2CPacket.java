package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ManaBallEffectS2CPacket {
    private final double x, y, z;
    private final int color;

    // 服务端构造
    public ManaBallEffectS2CPacket(Vec3 pos, int color) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.color = color;
    }

    // 服务端构造（直接传坐标）
    public ManaBallEffectS2CPacket(double x, double y, double z, int color) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
    }

    // 从字节流恢复
    public ManaBallEffectS2CPacket(FriendlyByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.color = buf.readInt();
    }

    // 序列化
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeInt(color);
    }

    // 客户端处理
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
          Vec3 vec3=new Vec3(x,y,z);
            ParticleOptions particleoptions = ParticleTypes.INSTANT_EFFECT;
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;
            RandomSource randomSource= null;
            if (Minecraft.getInstance().level != null) {
                randomSource = Minecraft.getInstance().level.random;
                for (int k2 = 0; k2 < 100; ++k2) {
                    double d13 = randomSource.nextDouble() * 4.0D;
                    double d19 = randomSource.nextDouble() * Math.PI * 2.0D;
                    double d25 = Math.cos(d19) * d13;
                    double d30 = 0.01D + randomSource.nextDouble() * 0.5D;
                    double d31 = Math.sin(d19) * d13;
                    Particle particle1 = addParticleInternal(particleoptions, particleoptions.getType().getOverrideLimiter(), vec3.x + d25 * 0.1D, vec3.y + 0.3D, vec3.z + d31 * 0.1D, d25, d30, d31);
                    if (particle1 != null) {
                        float f2 = 0.75F + randomSource.nextFloat() * 0.25F;
                        particle1.setColor(r * f2, g * f2, b * f2);
                        particle1.setPower((float) d13);
                    }
                }
            }

        });
        return true;
    }

    /**
     *从LevelRenderer照抄的方法，只是略微改了一下
     */
    @Nullable
    private Particle addParticleInternal(ParticleOptions pOptions, boolean pForce, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        return this.addParticleInternal(pOptions, pForce, false, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
    }

    @Nullable
    private Particle addParticleInternal(ParticleOptions pOptions, boolean pForce, boolean pDecreased, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        ParticleStatus particlestatus = this.calculateParticleLevel(pDecreased);
        if (pForce) {
            return Minecraft.getInstance().particleEngine.createParticle(pOptions, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        } else if (camera.getPosition().distanceToSqr(pX, pY, pZ) > 1024.0D) {
            return null;
        } else {
            return particlestatus == ParticleStatus.MINIMAL ? null : Minecraft.getInstance().particleEngine.createParticle(pOptions, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        }
    }

    private ParticleStatus calculateParticleLevel(boolean pDecreased) {
        ParticleStatus particlestatus = Minecraft.getInstance().options.particles().get();
        if (pDecreased && particlestatus == ParticleStatus.MINIMAL && Minecraft.getInstance().level.random.nextInt(10) == 0) {
            particlestatus = ParticleStatus.DECREASED;
        }

        if (particlestatus == ParticleStatus.DECREASED && Minecraft.getInstance().level.random.nextInt(3) == 0) {
            particlestatus = ParticleStatus.MINIMAL;
        }

        return particlestatus;
    }
}