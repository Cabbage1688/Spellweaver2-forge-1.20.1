package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TeleportParticleS2CPacket {
    private final Vec3 startPos;
    private final Vec3 targetPos;
    public TeleportParticleS2CPacket(Vec3 startPos, Vec3 targetPos){
        this.startPos=startPos;
        this.targetPos=targetPos;
    }
    // 从缓冲区读取数据（客户端接收时调用）
    public TeleportParticleS2CPacket(FriendlyByteBuf buf) {
        this.startPos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.targetPos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    // 将数据写入缓冲区（服务端发送时调用）
    public void toByte(FriendlyByteBuf buf) {
        buf.writeDouble(startPos.x);
        buf.writeDouble(startPos.y);
        buf.writeDouble(startPos.z);
        buf.writeDouble(targetPos.x);
        buf.writeDouble(targetPos.y);
        buf.writeDouble(targetPos.z);
    }

    // 客户端处理收到数据包
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return;

            //原坐标处生成特效
            spawnEnderParticlesAt(level,startPos.x,startPos.y,startPos.z);
            spawnEnderParticlesAt(level,startPos.x,startPos.y+1,startPos.z);
            //ParticleUtil.spawnEnderParticlesAt(world,packet.entityPos().x,packet.entityPos().y+2,packet.entityPos().z);
            //目标坐标处生成特效
            spawnEnderParticlesAt(level,targetPos.x,targetPos.y,targetPos.z);
            spawnEnderParticlesAt(level,targetPos.x,targetPos.y+1,targetPos.z);
        });
        return true;
    }

    //这个方法用于给传送符文的特效
    public  void spawnEnderParticlesAt(Level world, double x, double y, double z) {
        // 生成末影人紫色传送门粒子效果
        for (int i = 0; i < 48; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 2.0*3;
            double offsetY = (world.random.nextDouble() - 0.5) * 2.0*3;
            double offsetZ = (world.random.nextDouble() - 0.5) * 2.0*3;

            world.addParticle(
                    ParticleTypes.PORTAL, // 使用末影人传送门粒子
                    x, y, z,             // 粒子生成位置
                    offsetX * 0.1, offsetY * 0.1, offsetZ * 0.1 // 粒子速度
            );
        }

        // 添加一些紫颂果粒子效果增强视觉效果
        for (int i = 0; i < 5; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 1.5;
            double offsetY = (world.random.nextDouble() - 0.5) * 1.5;
            double offsetZ = (world.random.nextDouble() - 0.5) * 1.5;

            world.addParticle(
                    ParticleTypes.REVERSE_PORTAL, // 使用紫颂果粒子
                    x, y, z,                     // 粒子生成位置
                    offsetX * 0.05, offsetY * 0.05, offsetZ * 0.05 // 粒子速度
            );
        }
    }
}
