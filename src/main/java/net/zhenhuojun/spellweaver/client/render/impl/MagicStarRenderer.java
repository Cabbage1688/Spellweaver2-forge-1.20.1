package net.zhenhuojun.spellweaver.client.render.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.entity.impl.MagicStarEntity;
import net.zhenhuojun.spellweaver.entity.model.MagicStarModel;

/**
 * 魔法之星实体渲染器。
 * 使用 MobRenderer 复用 nametag、hurt flash、death 动画等机制。
 * 模型层 MagicStarModel.LAYER_LOCATION 须在客户端事件中注册。
 */
public class MagicStarRenderer extends MobRenderer<MagicStarEntity, MagicStarModel<MagicStarEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID, "textures/entity/mob/magic_star.png");

    public MagicStarRenderer(EntityRendererProvider.Context context) {
        // 阴影半径 0.3F（实体大小 0.5x0.5，影子稍小避免突兀）
        super(context, new MagicStarModel<>(context.bakeLayer(MagicStarModel.LAYER_LOCATION)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(MagicStarEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(MagicStarEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // FlyingMob 悬浮时上下微微波动（基于 tickCount）
        poseStack.pushPose();
        float bob = (float) (Math.sin((entity.tickCount + partialTicks) * 0.1F) * 0.05F);
        poseStack.translate(0.0F, bob-0.3, 0.0F);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override//亮度始终15，呈现发光效果
    protected int getBlockLightLevel(MagicStarEntity entity, BlockPos pos){
        return 15;
    }
}
