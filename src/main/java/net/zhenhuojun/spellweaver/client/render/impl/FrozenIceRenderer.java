package net.zhenhuojun.spellweaver.client.render.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.ModelData;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.entity.impl.FrozenIceEntity;

import java.util.UUID;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

public class FrozenIceRenderer extends EntityRenderer<FrozenIceEntity> {
    private static final ResourceLocation ICE_TEXTURE =
            fromNamespaceAndPath("minecraft","textures/block/ice.png");
    private static final ResourceLocation PACKED_ICE_TEXTURE =
            fromNamespaceAndPath("minecraft","textures/block/packed_ice.png");

    public FrozenIceRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FrozenIceEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {


        float width =entity.getWidth();
        float height =entity.getHeight();
        float depth =entity.getDepth();
;
        poseStack.pushPose();

        poseStack.translate(-0.5, -0.5, -0.5);

        //稍微放大一点以包裹实体
        float scaleX = width + 0.2f;
        float scaleY = height + 0.2f;
        float scaleZ = depth + 0.2f;
        poseStack.scale(scaleX, scaleY, scaleZ);
        //poseStack.translate(0, height / 2.0F, 0);

        // 获取冰块方块状态对应的模型
        BlockState iceState = Blocks.PACKED_ICE.defaultBlockState();
        BakedModel iceModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(iceState);

        // 渲染模型
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(RenderType.translucent()),
                iceState,
                iceModel,
                1.0F, 1.0F, 1.0F,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY,
                RenderType.solid()
        );

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }



    @Override
    public ResourceLocation getTextureLocation(FrozenIceEntity entity) {
        return PACKED_ICE_TEXTURE;
    }
}