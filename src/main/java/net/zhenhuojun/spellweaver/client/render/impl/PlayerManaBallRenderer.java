package net.zhenhuojun.spellweaver.client.render.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.entity.impl.ManaBall;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class PlayerManaBallRenderer extends EntityRenderer<ManaBall> {

    // 使用龙息弹的材质
    private static final ResourceLocation DRAGON_FIREBALL_TEXTURE =
           // new ResourceLocation("textures/entity/enderdragon/dragon_fireball.png");
    //ResourceLocation.fromNamespaceAndPath("minecraft","textures/entity/enderdragon/dragon_fireball.png");
    ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID,"textures/entity/projectiles/mana_ball.png");

    // 渲染类型 - 使用实体透明渲染
    private static final RenderType RENDER_TYPE =
            RenderType.entityCutoutNoCull(DRAGON_FIREBALL_TEXTURE);

    public PlayerManaBallRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ManaBall entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // 根据实体大小调整缩放,先不写了
        //float scale = (float) entity.getR();
        poseStack.scale(1, 1, 1);

        // 朝向摄像机，保持这个以确保始终面向玩家
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());


        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RENDER_TYPE);


        vertex(vertexConsumer, poseMatrix, normalMatrix, packedLight, 0.0F, 0, 0, 1);
        vertex(vertexConsumer, poseMatrix, normalMatrix, packedLight, 1.0F, 0, 1, 1);
        vertex(vertexConsumer, poseMatrix, normalMatrix, packedLight, 1.0F, 1, 1, 0);
        vertex(vertexConsumer, poseMatrix, normalMatrix, packedLight, 0.0F, 1, 0, 0);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f poseMatrix, Matrix3f normalMatrix,
                               int packedLight, float x, int y, int u, int v) {
        consumer.vertex(poseMatrix, x - 0.5F, (float)y - 0.25F, 0.0F)
                .color(255, 255, 255, 255)
                .uv((float)u, (float)v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normalMatrix, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(ManaBall entity) {
        return DRAGON_FIREBALL_TEXTURE;
    }
}