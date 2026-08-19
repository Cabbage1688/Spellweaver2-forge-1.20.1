package net.zhenhuojun.spellweaver.entity.model;// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.zhenhuojun.spellweaver.Spellweaver;

public class MagicStarModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID, "magic_star"), "main");
	private final ModelPart core;
	private final ModelPart top;
	private final ModelPart bottom;
	private final ModelPart side1;
	private final ModelPart side2;

	public MagicStarModel(ModelPart root) {
		this.core = root.getChild("core");
		this.top = root.getChild("top");
		this.bottom = root.getChild("bottom");
		this.side1 = root.getChild("side1");
		this.side2 = root.getChild("side2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition core = partdefinition.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 17.0F, 2.0F));

		PartDefinition top = partdefinition.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 10).addBox(-2.0F, -13.0F, -2.0F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(20, 14).addBox(-1.0F, -14.0F, -1.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(12, 24).addBox(0.0F, -15.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition bottom = partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 17).addBox(-2.0F, -16.0F, -2.0F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(20, 18).addBox(-1.0F, -14.0F, -1.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(16, 24).addBox(0.0F, -13.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 34.0F, 0.0F));

		PartDefinition side1 = partdefinition.addOrReplaceChild("side1", CubeListBuilder.create().texOffs(20, 0).addBox(-2.0F, -13.0F, -2.0F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(20, 22).addBox(-1.0F, -14.0F, -1.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(12, 26).addBox(0.0F, -3.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, 15.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition side2 = partdefinition.addOrReplaceChild("side2", CubeListBuilder.create().texOffs(16, 26).addBox(6.0F, -9.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r1 = side2.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 24).addBox(-1.0F, -1.0F, -1.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -9.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition cube_r2 = side2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(20, 7).addBox(-2.0F, -2.0F, -2.0F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -9.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		core.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		top.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		side1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		side2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}