package net.zhenhuojun.spellweaver.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerManaOverloadProvider;
import net.zhenhuojun.spellweaver.client.data_util.ClientPlayerOverloadData;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

@Mod.EventBusSubscriber(modid = Spellweaver.MODID, value = Dist.CLIENT)
public class OverloadRenderer {

    private static final ResourceLocation SEQUENCE_TEXTURE =
            fromNamespaceAndPath(Spellweaver.MODID, "textures/gui/sequence_node.png");

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        //Spellweaver.getLOGGER().debug("[Spellweaver:OverloadRenderer/onRenderLevelStage方法]事件触发");
        // 在粒子渲染之后进行自定义渲染
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
           // Spellweaver.getLOGGER().debug("[Spellweaver:OverloadRenderer/onRenderLevelStage方法]条件不满足");
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        // 基础条件判断
        if (player == null || mc.options.getCameraType().isFirstPerson()) {
            //Spellweaver.getLOGGER().debug("[Spellweaver:OverloadRenderer/onRenderLevelStage方法]玩家不存在或为第一人称");
            return;
        }


            if(!ClientPlayerOverloadData.isEnabled()){
                //Spellweaver.getLOGGER().debug("[Spellweaver:OverloadRenderer/onRenderLevelStage方法]魔力超载未启用，不渲染特效");
                return;
            }

            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
            float partialTick = event.getPartialTick();

            //  计算动画旋转角度 (每秒转180度)
            long gameTime = System.currentTimeMillis();
            float rotationAngle = (gameTime % 3600L) / 3600.0f * 360.0f;

            // 执行渲染
            renderOverloadEffect(player, poseStack, bufferSource, partialTick, rotationAngle);
        //Spellweaver.getLOGGER().debug("[Spellweaver:OverloadRenderer/onRenderLevelStage方法]特效渲染完毕");
       // });
    }

    private static void renderOverloadEffect(Player player, PoseStack poseStack,
                                             MultiBufferSource.BufferSource bufferSource,
                                             float partialTick, float rotationAngle) {
       // Spellweaver.getLOGGER().debug("[Spellweaver:OverloadRenderer/renderOverloadEffect方法]renderOverloadEffect方法被调用");
        poseStack.pushPose();

        // --- 1. 定位到玩家位置 ---
        double x = player.xOld + (player.getX() - player.xOld) * partialTick;
        double y = player.yOld + (player.getY() - player.yOld) * partialTick + 1.5; // 在玩家胸部高度
        double z = player.zOld + (player.getZ() - player.zOld) * partialTick;
        poseStack.translate(x, y, z);

        // --- 2. 旋转使贴图始终面向相机 (公告板效果) ---
        Minecraft mc = Minecraft.getInstance();
        Quaternionf cameraRotation = mc.gameRenderer.getMainCamera().rotation();
        poseStack.mulPose(cameraRotation);

        // --- 3. 向后平移，使其显示在玩家背后 ---
        // 注意：因为已经旋转到相机朝向，现在Z轴就是视线方向，我们需要沿Z轴反方向平移
        float distanceFromPlayer = 0.8f; // 距离玩家背后的距离
        poseStack.translate(0.0f, 0.0f, distanceFromPlayer);

        // --- 4. 在相机朝向上叠加自转 ---
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotationAngle));

        // --- 5. 设置缩放 ---
        float scale = 0.5f;
        poseStack.scale(scale, scale, scale);

        // --- 6. 获取渲染缓冲并绘制 ---
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(SEQUENCE_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        int light = 15728880; // 最大亮度

        // 绘制一个面向相机的平面 (中心在原点，范围从 -0.5 到 +0.5)
        float halfSize = 0.5f;
        // 顶点1: 左上角 (U=0, V=0)
        vertexConsumer.vertex(matrix, -halfSize,  halfSize, 0).color(255, 255, 255, 255).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
        // 顶点2: 左下角 (U=0, V=1)
        vertexConsumer.vertex(matrix, -halfSize, -halfSize, 0).color(255, 255, 255, 255).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
        // 顶点3: 右下角 (U=1, V=1)
        vertexConsumer.vertex(matrix,  halfSize, -halfSize, 0).color(255, 255, 255, 255).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
        // 顶点4: 右上角 (U=1, V=0)
        vertexConsumer.vertex(matrix,  halfSize,  halfSize, 0).color(255, 255, 255, 255).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();

        poseStack.popPose();

        // 7. 立即提交渲染 (确保效果正确叠加)
        bufferSource.endBatch();
    }
}