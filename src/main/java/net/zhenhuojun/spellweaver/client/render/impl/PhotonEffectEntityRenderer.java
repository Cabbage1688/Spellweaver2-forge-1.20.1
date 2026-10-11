package net.zhenhuojun.spellweaver.client.render.impl;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.zhenhuojun.spellweaver.entity.impl.PhotonEffectEntity;

public class PhotonEffectEntityRenderer extends EntityRenderer<PhotonEffectEntity> {

    public PhotonEffectEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public ResourceLocation getTextureLocation(PhotonEffectEntity pEntity) {
        return null;
    }

}
