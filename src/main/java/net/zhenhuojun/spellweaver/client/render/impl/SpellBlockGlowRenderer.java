package net.zhenhuojun.spellweaver.client.render.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.zhenhuojun.spellweaver.client.data_util.ClientSpellBlockData;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Set;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class SpellBlockGlowRenderer {

    private static final float GLOW_R = 1.0F;
    private static final float GLOW_G = 1.0F;
    private static final float GLOW_B = 1.0F;
    private static final float GLOW_ALPHA = 0.8F;
    private static final float OUTLINE_OFFSET = 0.002F;

    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Set<BlockPos> spellBlocks = ClientSpellBlockData.getSpellBlocks();
        if (spellBlocks.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();

        float partialTicks = event.getPartialTick();
        float time = (float) mc.level.getGameTime() + partialTicks;

        poseStack.pushPose();

        for (BlockPos pos : spellBlocks) {
            double x = pos.getX() - camPos.x;
            double y = pos.getY() - camPos.y;
            double z = pos.getZ() - camPos.z;

            poseStack.pushPose();
            poseStack.translate(x, y, z);

            BlockState state = mc.level.getBlockState(pos);
            renderGlowBorder(poseStack, bufferSource, time, state,mc.level, pos);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void renderGlowBorder(PoseStack poseStack, MultiBufferSource bufferSource, float time, BlockState state, Level level, BlockPos pos) {
        RenderType renderType = RenderType.lines();
        VertexConsumer vc = bufferSource.getBuffer(renderType);

        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        float alpha = GLOW_ALPHA + Mth.sin(time * 0.05F) * 0.2F;
        float r = GLOW_R;
        float g = GLOW_G;
        float b = GLOW_B;

        VoxelShape shape = state.getShape((BlockGetter) level, pos);
        for (AABB aabb : shape.toAabbs()) {
            float minX = (float) aabb.minX - OUTLINE_OFFSET;
            float minY = (float) aabb.minY - OUTLINE_OFFSET;
            float minZ = (float) aabb.minZ - OUTLINE_OFFSET;
            float maxX = (float) aabb.maxX + OUTLINE_OFFSET;
            float maxY = (float) aabb.maxY + OUTLINE_OFFSET;
            float maxZ = (float) aabb.maxZ + OUTLINE_OFFSET;

            renderLine(vc, matrix, normal, minX, minY, minZ, maxX, minY, minZ, r, g, b, alpha);
            renderLine(vc, matrix, normal, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, alpha);
            renderLine(vc, matrix, normal, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, alpha);
            renderLine(vc, matrix, normal, minX, minY, maxZ, minX, minY, minZ, r, g, b, alpha);

            renderLine(vc, matrix, normal, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, alpha);
            renderLine(vc, matrix, normal, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, alpha);
            renderLine(vc, matrix, normal, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, alpha);
            renderLine(vc, matrix, normal, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, alpha);

            renderLine(vc, matrix, normal, minX, minY, minZ, minX, maxY, minZ, r, g, b, alpha);
            renderLine(vc, matrix, normal, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, alpha);
            renderLine(vc, matrix, normal, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, alpha);
            renderLine(vc, matrix, normal, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, alpha);
        }
    }

    private static void renderLine(VertexConsumer vc, Matrix4f matrix, Matrix3f normalMatrix,
                                   float x1, float y1, float z1,
                                   float x2, float y2, float z2,
                                   float r, float g, float b, float a) {
        Vector3f direction = new Vector3f(x2 - x1, y2 - y1, z2 - z1);
        float length = direction.length();
        if (length > 0) {
            direction.div(length);
        }

        vc.vertex(matrix, x1, y1, z1).color(r, g, b, a)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880)
                .normal(normalMatrix, direction.x(), direction.y(), direction.z()).endVertex();
        vc.vertex(matrix, x2, y2, z2).color(r, g, b, a)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880)
                .normal(normalMatrix, direction.x(), direction.y(), direction.z()).endVertex();
    }
}