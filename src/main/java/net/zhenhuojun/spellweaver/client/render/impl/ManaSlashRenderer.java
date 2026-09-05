package net.zhenhuojun.spellweaver.client.render.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.entity.ModEntities;
import net.zhenhuojun.spellweaver.entity.impl.ManaSlashEntity;
import net.zhenhuojun.spellweaver.entity.model.ManaSlash;
import org.jetbrains.annotations.NotNull;

/**
 * 魔法剑气渲染器。
 * 使用 ManaSlash 模型（Blockbench 实体模型，模型空间正面为 -Z），
 * 模型层 ManaSlash.LAYER_LOCATION 须在客户端事件中注册。
 */
public class ManaSlashRenderer extends EntityRenderer<ManaSlashEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID, "textures/entity/projectiles/mana_slash.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE);

    private final ManaSlash<ManaSlashEntity> model;

    public ManaSlashRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ManaSlash<>(context.bakeLayer(ManaSlash.LAYER_LOCATION));
    }

    @Override
    public void render(ManaSlashEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();


        float scale = 1.0f;
        EntityType<?> type = entity.getType();
        if (type == ModEntities.MANA_SLASH.get()) {
            scale = 1.0f;
        } else if (type == ModEntities.MANA_SLASH_MEDIUM.get()) {
            scale = 2.0f;
        } else if (type == ModEntities.MANA_SLASH_LARGE.get()) {
            scale = 3.0f;
        }
        poseStack.scale(scale, scale, scale);

        // Blockbench 实体模型以 y=24 为原点，补偿偏移使剑气居中于实体位置
        poseStack.translate(0.0F, -1.501F, 0.0F);
        // 朝向飞行方向（三维对齐，含俯仰）：
        // 优先用实际速度向量计算朝向（原版 lookAt 同款换算），剑气飞向哪就朝向哪，
        // 完全不依赖实体旋转的同步与插值，杜绝可见的旋转过渡；
        // 刚生成时客户端速度尚未同步（为 0），回退到生成包携带的 yaw/pitch 原始值
        Vec3 motion = entity.getDeltaMovement();
        float yaw;
        float pitch;
        if (motion.lengthSqr() > 1.0E-4D) {
            yaw = (float) (Mth.atan2(motion.z, motion.x) * (180.0 / Math.PI)) - 90.0F;
            pitch = (float) (-(Mth.atan2(motion.y, motion.horizontalDistance()) * (180.0 / Math.PI)));
        } else {
            yaw = entity.getYRot();
            pitch = entity.getXRot();
        }
        // 矩阵右乘：先绕模型 X 轴俯仰倾斜，再偏航对齐水平飞行方向。
        // 模型剑刃（弧形凸边）朝模型 -Z，故偏航加 180° 让剑刃领先指向飞行方向（背向玩家）；
        // 偏航翻转后俯仰轴随之反向，pitch 取反
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw + 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));

        VertexConsumer consumer = buffer.getBuffer(RENDER_TYPE);
        model.setupAnim(entity, partialTicks, 0.0F, entity.tickCount + partialTicks, 0.0F, 0.0F);
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ManaSlashEntity entity) {
        return TEXTURE;
    }

    @Override//亮度始终15，剑气呈现发光效果
    protected int getBlockLightLevel(@NotNull ManaSlashEntity entity, @NotNull BlockPos pos) {
        return 15;
    }
}
