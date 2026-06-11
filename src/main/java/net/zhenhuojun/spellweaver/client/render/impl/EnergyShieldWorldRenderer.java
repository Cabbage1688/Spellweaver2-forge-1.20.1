package net.zhenhuojun.spellweaver.client.render.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.zhenhuojun.spellweaver.client.gui.util.ClientManaShieldData;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class EnergyShieldWorldRenderer {

    private static final ResourceLocation ENERGY_ARMOR_TEXTURE =
             ResourceLocation.fromNamespaceAndPath("minecraft","textures/entity/creeper/creeper_armor.png");


    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        if (ClientManaShieldData.getShieldAmount() <= 0) return;

        //第一人称视角下不显示自己的护盾以避免光污染，太tm炫了
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) {
            return;
        }

        float partialTicks = event.getPartialTick();
        float time = (float) player.tickCount + partialTicks;

        // 玩家平滑世界坐标（脚底位置）
        double x = Mth.lerp(partialTicks, player.xOld, player.getX());
        double y = Mth.lerp(partialTicks, player.yOld, player.getY());
        double z = Mth.lerp(partialTicks, player.zOld, player.getZ());

        // 3x3x3 立方体，以玩家脚底为中心
        //float halfSize = 1.5F;
        //还是别3*3了，太tm大了
        float halfSize = 0.95F;
        double minX = x - halfSize;
        //double minY = y - halfSize+1.25;
        double minY = y - halfSize+1.0;
        double minZ = z - halfSize;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();

        poseStack.pushPose();
        // 平移到立方体最小角（世界坐标 -> 视口坐标）
        poseStack.translate(minX - camPos.x, minY - camPos.y, minZ - camPos.z);

        // 外层能量罩
        renderEnergyCube(poseStack, bufferSource, time, halfSize * 2, true);
        // 内层能量罩
        //renderEnergyCube(poseStack, bufferSource, time, halfSize * 2 * 0.85F, false);

        poseStack.popPose();
        // bufferSource 会在事件结束后统一提交，这里不需要手动 endBatch
    }

    private static void renderEnergyCube(PoseStack poseStack, MultiBufferSource bufferSource,
                                         float time, float size, boolean outer) {
        // 两层使用不同的纹理滚动速度与偏移
        RenderType renderType = RenderType.energySwirl(
                ENERGY_ARMOR_TEXTURE,
                (time * 0.01F) % 1.0F,
                (time * (outer ? 0.01F : 0.02F)) % 1.0F
        );
        VertexConsumer vc = bufferSource.getBuffer(renderType);

        float r, g, b, alpha;
        if (outer) {
            r = 0.2F;
            g = 0.4F + Mth.sin(time * 0.3F) * 0.3F;
            b = 0.9F + Mth.cos(time * 0.2F) * 0.1F;
            alpha = 0.7F + Mth.sin(time * 0.25F) * 0.3F;
        } else {
            r = 0.1F;
            g = 0.7F + Mth.cos(time * 0.4F) * 0.2F;
            b = 1.0F;
            alpha = 0.4F + Mth.sin(time * 0.5F) * 0.2F;
        }

        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        // 立方体范围从 (0,0,0) 到 (size, size, size)
        float x0 = 0, y0 = 0, z0 = 0;
        float x1 = size, y1 = size, z1 = size;

        // 六个面，带动态纹理坐标
        float uOff = time * 0.02F % 1.0F;
        float vOff = time * 0.03F % 1.0F;

        // 上面
        addFace(vc, matrix, normal, x0, y1, z0, x1, y1, z1, Direction.UP,    r, g, b, alpha, uOff, vOff);
        // 下面
        addFace(vc, matrix, normal, x0, y0, z0, x1, y0, z1, Direction.DOWN,  r, g, b, alpha, uOff, vOff);
        // 北面
        addFace(vc, matrix, normal, x0, y0, z0, x1, y1, z0, Direction.NORTH, r, g, b, alpha, uOff, vOff);
        // 南面
        addFace(vc, matrix, normal, x0, y0, z1, x1, y1, z1, Direction.SOUTH, r, g, b, alpha, uOff, vOff);
        // 西面
        addFace(vc, matrix, normal, x0, y0, z0, x0, y1, z1, Direction.WEST,  r, g, b, alpha, uOff, vOff);
        // 东面
        addFace(vc, matrix, normal, x1, y0, z0, x1, y1, z1, Direction.EAST,  r, g, b, alpha, uOff, vOff);
    }

    private static void addFace(VertexConsumer vc, Matrix4f matrix, Matrix3f normal,
                                float x0, float y0, float z0, float x1, float y1, float z1,
                                Direction dir, float r, float g, float b, float a,
                                float uOff, float vOff) {
        // 顶点和纹理坐标根据方向生成
        switch (dir) {
            case UP -> {
                vc.vertex(matrix, x0, y1, z1).color(r, g, b, a).uv(uOff, vOff + 1)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 1, 0).endVertex();
                vc.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(uOff + 1, vOff + 1)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 1, 0).endVertex();
                vc.vertex(matrix, x1, y1, z0).color(r, g, b, a).uv(uOff + 1, vOff)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 1, 0).endVertex();
                vc.vertex(matrix, x0, y1, z0).color(r, g, b, a).uv(uOff, vOff)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 1, 0).endVertex();
            }
            case DOWN -> {
                vc.vertex(matrix, x0, y0, z0).color(r, g, b, a).uv(uOff, vOff + 1)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, -1, 0).endVertex();
                vc.vertex(matrix, x1, y0, z0).color(r, g, b, a).uv(uOff + 1, vOff + 1)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, -1, 0).endVertex();
                vc.vertex(matrix, x1, y0, z1).color(r, g, b, a).uv(uOff + 1, vOff)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, -1, 0).endVertex();
                vc.vertex(matrix, x0, y0, z1).color(r, g, b, a).uv(uOff, vOff)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, -1, 0).endVertex();
            }
            case NORTH -> {
                vc.vertex(matrix, x0, y1, z0).color(r, g, b, a).uv(uOff + 1, vOff)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, -1).endVertex();
                vc.vertex(matrix, x1, y1, z0).color(r, g, b, a).uv(uOff, vOff)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, -1).endVertex();
                vc.vertex(matrix, x1, y0, z0).color(r, g, b, a).uv(uOff, vOff + 1)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, -1).endVertex();
                vc.vertex(matrix, x0, y0, z0).color(r, g, b, a).uv(uOff + 1, vOff + 1)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, -1).endVertex();
            }
            case SOUTH -> {
                vc.vertex(matrix, x0, y0, z1).color(r, g, b, a).uv(uOff, vOff + 1)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, 1).endVertex();
                vc.vertex(matrix, x1, y0, z1).color(r, g, b, a).uv(uOff + 1, vOff + 1)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, 1).endVertex();
                vc.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(uOff + 1, vOff)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, 1).endVertex();
                vc.vertex(matrix, x0, y1, z1).color(r, g, b, a).uv(uOff, vOff)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, 1).endVertex();
            }
            case WEST -> {
                vc.vertex(matrix, x0, y0, z0).color(r, g, b, a).uv(uOff, vOff + 1)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, -1, 0, 0).endVertex();
                vc.vertex(matrix, x0, y0, z1).color(r, g, b, a).uv(uOff + 1, vOff + 1)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, -1, 0, 0).endVertex();
                vc.vertex(matrix, x0, y1, z1).color(r, g, b, a).uv(uOff + 1, vOff)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, -1, 0, 0).endVertex();
                vc.vertex(matrix, x0, y1, z0).color(r, g, b, a).uv(uOff, vOff)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, -1, 0, 0).endVertex();
            }
            case EAST -> {
                vc.vertex(matrix, x1, y1, z0).color(r, g, b, a).uv(uOff, vOff)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 1, 0, 0).endVertex();
                vc.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(uOff + 1, vOff)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 1, 0, 0).endVertex();
                vc.vertex(matrix, x1, y0, z1).color(r, g, b, a).uv(uOff + 1, vOff + 1)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 1, 0, 0).endVertex();
                vc.vertex(matrix, x1, y0, z0).color(r, g, b, a).uv(uOff, vOff + 1)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 1, 0, 0).endVertex();
            }
        }
    }
}