package net.zhenhuojun.spellweaver.client.render.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.entity.impl.SpellEffectEntity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
/// 废案
public class SpellEffectRenderer extends EntityRenderer<SpellEffectEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID, "textures/gui/sequence_node.png");

    public SpellEffectRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(SpellEffectEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // 1. 获取玩家（特效的主人）
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        // 2. 计算插值位置（让特效平滑跟随）
        double x = player.xOld + (player.getX() - player.xOld) * partialTick;
        double y = player.yOld + (player.getY() - player.yOld) * partialTick + 1.2; // 胸部高度
        double z = player.zOld + (player.getZ() - player.zOld) * partialTick;

        poseStack.pushPose();
        // 3. 移动到玩家背后位置（注意这里使用的是世界坐标偏移，不需要相机旋转）
        //    但是为了让贴图始终面向相机，我们需要 Billboard 变换。
        poseStack.translate(x, y, z);

        // 4. Billboard 旋转（直接面向相机）
        Minecraft mc = Minecraft.getInstance();
        Quaternionf cameraRot = mc.getEntityRenderDispatcher().cameraOrientation();
        poseStack.mulPose(cameraRot);

        // 5. 向后平移（沿视线方向反方向，即远离相机）
        //    注意：此时 Z 轴已经是视线方向，正方向是远离相机还是朝向相机？经过测试：
        //    相机四元数旋转后，Z 轴正方向是**朝向相机**，所以需要沿 Z 负方向平移。
        poseStack.translate(0, 0, -0.8f);

        // 6. 自转（绕 Z 轴）
        long gameTime = System.currentTimeMillis();
        float rotationAngle = (gameTime % 3600L) / 3600.0f * 360.0f;
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotationAngle));

        // 7. 缩放
        float scale = 0.5f;
        poseStack.scale(scale, scale, scale);

        // 8. 绘制纹理平面（代码与你之前相同）
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        float halfSize = 0.5f;
        vertexConsumer.vertex(matrix, -halfSize,  halfSize, 0)
                .color(255, 255, 255, 255).uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(0,0,1).endVertex();
        vertexConsumer.vertex(matrix, -halfSize, -halfSize, 0)
                .color(255, 255, 255, 255).uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(0,0,1).endVertex();
        vertexConsumer.vertex(matrix,  halfSize, -halfSize, 0)
                .color(255, 255, 255, 255).uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(0,0,1).endVertex();
        vertexConsumer.vertex(matrix,  halfSize,  halfSize, 0)
                .color(255, 255, 255, 255).uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(0,0,1).endVertex();

        poseStack.popPose();
        // 不需要手动 endBatch，让渲染系统自动提交
    }

    @Override
    public ResourceLocation getTextureLocation(SpellEffectEntity pEntity) {
        return TEXTURE;
    }
}
