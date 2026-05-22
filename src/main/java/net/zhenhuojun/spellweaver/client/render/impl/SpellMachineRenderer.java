package net.zhenhuojun.spellweaver.client.render.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.block.custom.SpellMachineBlockEntity;
import net.zhenhuojun.spellweaver.item.ModItems;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

public class SpellMachineRenderer implements BlockEntityRenderer<SpellMachineBlockEntity> {
    private static final ResourceLocation SEQUENCE_TEXTURE = fromNamespaceAndPath(Spellweaver.MODID, "textures/gui/sequence_node.png");

    private final ItemRenderer itemRenderer;

    public SpellMachineRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(SpellMachineBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        int bottleCount = blockEntity.getCurrentManaBottle();
        if (bottleCount <= 0) {
            return;
        }

        ItemStack bottleStack = new ItemStack(ModItems.MANA_BOTTLE.get());


        for (int i = 0; i < bottleCount; i++) {
            poseStack.pushPose();

            //调整位置，让瓶子均匀分布在机器上方
            double offsetX = (i - 1) * 0.3f;
            //调整瓶子的渲染大小
            switch(i){
                case 0, 2 ->poseStack.translate(0.5 + offsetX, 0.35, 0.75);
                case 1->poseStack.translate(0.5 + offsetX, 0.35, 0.25);
            }
             poseStack.scale(0.5f, 0.5f, 0.5f);

            // ItemDisplayContext.NONE 会以普通方式渲染，不强制特定视角或角度
            this.itemRenderer.renderStatic(bottleStack, ItemDisplayContext.NONE, packedLight, packedOverlay, poseStack, bufferSource, blockEntity.getLevel(), 0);

            poseStack.popPose();
        }

        /*if (blockEntity.isCasting()) {
            renderRotatingNode(poseStack, bufferSource, partialTick, packedLight, packedOverlay);
        }

         */
        if(blockEntity.getSpellRoot()!=null&& !blockEntity.getSpellRoot().getChildrenNodeList().isEmpty()){
            renderRotatingNode(poseStack, bufferSource, partialTick, packedLight, packedOverlay);
        }
    }

    private void renderRotatingNode(PoseStack poseStack, MultiBufferSource bufferSource,
                                    float partialTick, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        // 移动到机器顶部的中心
        poseStack.translate(0.5, 0.85+0.2+0.1, 0.5);

        // 计算旋转角度：速度 * 时间，加上 partialTick 让动画更平滑
        //float speed = 45.0F; // 每秒旋转45度，可根据需要调整
        float speed = 15.0F;
        //float angle = (System.currentTimeMillis() % 360000) * 0.001F * speed + partialTick * speed;
        float angle = (System.currentTimeMillis() % 360000) * 0.001f*10;
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));


        // 绘制一个边长为 0.4 的正方形平面（水平，位于 XZ 平面）
        //float halfSize = 0.2F;
        float halfSize = 0.4F;
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(SEQUENCE_TEXTURE));

        // 法线向上 (0, 1, 0)
        vertexConsumer.vertex(matrix, -halfSize, 0, -halfSize).color(255, 255, 255, 255)
                .uv(0, 0).overlayCoords(packedOverlay).uv2(packedLight).normal(normal, 0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix, halfSize, 0, -halfSize).color(255, 255, 255, 255)
                .uv(1, 0).overlayCoords(packedOverlay).uv2(packedLight).normal(normal, 0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix, halfSize, 0, halfSize).color(255, 255, 255, 255)
                .uv(1, 1).overlayCoords(packedOverlay).uv2(packedLight).normal(normal, 0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix, -halfSize, 0, halfSize).color(255, 255, 255, 255)
                .uv(0, 1).overlayCoords(packedOverlay).uv2(packedLight).normal(normal, 0, 1, 0).endVertex();

        poseStack.popPose();
    }
}