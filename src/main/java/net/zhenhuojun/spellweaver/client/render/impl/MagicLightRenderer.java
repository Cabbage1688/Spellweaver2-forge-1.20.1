package net.zhenhuojun.spellweaver.client.render.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.zhenhuojun.spellweaver.entity.impl.MagicLightEntity;

//魔法光源渲染器
public class MagicLightRenderer extends EntityRenderer<MagicLightEntity> {

    public MagicLightRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    @Override
    public void render(MagicLightEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // 不需要渲染模型，粒子效果已在实体中生成
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(MagicLightEntity magicLightEntity) {
        return null;//没有纹理，直接null
    }

}