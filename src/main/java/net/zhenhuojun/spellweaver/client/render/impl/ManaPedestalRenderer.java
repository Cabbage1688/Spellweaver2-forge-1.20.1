package net.zhenhuojun.spellweaver.client.render.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.zhenhuojun.spellweaver.block.custom.ManaPedestalBlockEntity;
import net.zhenhuojun.spellweaver.item.ModItems;

public class ManaPedestalRenderer implements BlockEntityRenderer<ManaPedestalBlockEntity> {

    private final ItemRenderer itemRenderer;

    public ManaPedestalRenderer(BlockEntityRendererProvider.Context context){
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ManaPedestalBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        int bottleCount = blockEntity.getCurrentManaBottle();
        if (bottleCount <= 0) {
            return;
        }

        ItemStack bottleStack = new ItemStack(ModItems.MANA_BOTTLE.get());
        
        for (int i = 0; i < bottleCount; i++) {
            poseStack.pushPose();

            double offsetX = (i%3 - 1) * 0.3f;

            if(i<=2){
                poseStack.translate(0.5 + offsetX, 0.50, 0.80);
            }else if(i<=5){
                poseStack.translate(0.5 + offsetX, 0.50, 0.5);
            }else {
                poseStack.translate(0.5 + offsetX, 0.50, 0.2);
            }

            poseStack.scale(0.40f, 0.40f, 0.40f);

            this.itemRenderer.renderStatic(bottleStack, ItemDisplayContext.NONE, packedLight, packedOverlay, poseStack, bufferSource, blockEntity.getLevel(), 0);

            poseStack.popPose();
        }

    }
}
