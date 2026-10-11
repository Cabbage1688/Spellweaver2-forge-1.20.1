package net.zhenhuojun.spellweaver.network.packet;

import com.lowdragmc.photon.client.fx.BlockEffect;
import com.lowdragmc.photon.client.fx.EntityEffect;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.spell.util.EffectType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
/// 当实体别移除后，绑定在其上的发射器一并移除，但已存在的粒子不会，需要等它寿命用完。
public class PhotonEffectS2CPacket {

    private final ResourceLocation fxLocation;
    private final ResourceKey<Level> dimension;
    private final EffectType effectType;

    //仅 BLOCK 类型时非空
    @Nullable
    private final BlockPos pos;
    //仅 ENTITY 类型时有效，-1 表示无
    private final int entityId;


    public PhotonEffectS2CPacket(ResourceLocation r, Level level, BlockPos pos, EffectType effectType) {
        this.fxLocation = r;
        this.dimension  = level.dimension();
        this.effectType = effectType;
        this.pos        = pos;
        this.entityId   = -1;
    }


    public PhotonEffectS2CPacket(ResourceLocation r, Level level, Entity entity, EffectType effectType) {
        this.fxLocation = r;
        this.dimension  = level.dimension();
        this.effectType = effectType;
        this.pos        = null;
        this.entityId   = entity.getId();
    }


    public PhotonEffectS2CPacket(FriendlyByteBuf buf) {
        this.fxLocation = buf.readResourceLocation();
        this.dimension  = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        this.effectType = buf.readEnum(EffectType.class);

        boolean hasPos = buf.readBoolean();
        this.pos = hasPos ? buf.readBlockPos() : null;
        this.entityId = buf.readInt();
    }


    public void toByte(FriendlyByteBuf buf) {
        buf.writeResourceLocation(fxLocation);
        buf.writeResourceLocation(dimension.location());
        buf.writeEnum(effectType);

        buf.writeBoolean(pos != null);
        if (pos != null) {
            buf.writeBlockPos(pos);
        }
        buf.writeInt(entityId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            FX fx= FXHelper.getFX(fxLocation);
            Level level= Minecraft.getInstance().level;
            switch (effectType){
                case NORMAL_BLOCK_EFFECT-> {
                    if(pos!=null&&fx!=null&&level != null && level.dimension() == dimension){
                        BlockEffect photonEffect = new BlockEffect(fx, level, pos);
                        photonEffect.start();
                        Spellweaver.getLOGGER().debug("[Spellweaver/PhotonEffectS2CPacket]已处理普通方块特效包");
                    }
                }
                case NORMAL_ENTITY_EFFECT -> {
                    if (level != null) {
                        Entity entity= level.getEntity(entityId);
                        if(entity!=null&&fx!=null&& level.dimension() == dimension){
                            EntityEffect entityEffect=new EntityEffect(fx,level,entity, EntityEffect.AutoRotate.NONE);
                            entityEffect.setOffset(0,(double) entity.getBbHeight()/2,0);
                            entityEffect.start();
                        }
                    }
                }
                case CHARGE -> {
                    if (level != null) {
                        Entity entity= level.getEntity(entityId);
                        if(entity!=null&&fx!=null&& level.dimension() == dimension){
                            EntityEffect entityEffect=new EntityEffect(fx,level,entity, EntityEffect.AutoRotate.NONE);
                            entityEffect.setOffset(0,0.1,0);
                            entityEffect.start();
                            entityEffect.setForcedDeath(true);//实体死亡则立即清除粒子，即使还有寿命
                            Spellweaver.getLOGGER().debug("[Spellweaver/PhotonEffectS2CPacket]已处理充能特效包");
                        }
                    }
                }
                case INSTANT_DEAD_ENTITY_EFFECT -> {
                    if (level != null) {
                        Entity entity= level.getEntity(entityId);
                        if(entity!=null&&fx!=null&& level.dimension() == dimension){
                            EntityEffect entityEffect=new EntityEffect(fx,level,entity, EntityEffect.AutoRotate.NONE);
                            entityEffect.setOffset(0,(double) entity.getBbHeight()/2,0);
                            entityEffect.setForcedDeath(true);
                            entityEffect.start();
                        }
                    }
                }
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}