package com.neoloxal.neospuppets.client;// Made with Blockbench 5.1.5
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class PuppetModel {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("neospuppets", "puppetmodel"), "main");
	private ModelPart waist;
	private ModelPart head;
	private ModelPart body;
	private ModelPart rightArm;
	private ModelPart leftArm;
	private ModelPart rightLeg;
	private ModelPart leftLeg;

	public PuppetModel(ModelPart root) {
		this.waist = root.getChild("Waist");
		this.head = this.waist.getChild("Head");
		this.body = this.waist.getChild("Body");
		this.rightArm = this.waist.getChild("Right Arm");
		this.leftArm = this.waist.getChild("Left Arm");
		this.rightLeg = root.getChild("Right Leg");
		this.leftLeg = root.getChild("Left Leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition waist = partdefinition.addOrReplaceChild("Waist", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));

		PartDefinition head = waist.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(16, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition body = waist.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(8, 8).addBox(-2.0F, -6.0F, -1.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(8, 16).addBox(-2.0F, -6.0F, -1.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition rightArm = waist.addOrReplaceChild("Right Arm", CubeListBuilder.create().texOffs(20, 8).addBox(-1.5F, -5.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(-1.5F, -5.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.1F)), PartPose.offset(-2.5F, -1.0F, 0.0F));

		PartDefinition leftArm = waist.addOrReplaceChild("Left Arm", CubeListBuilder.create().texOffs(16, 24).addBox(-0.5F, -5.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(24, 24).addBox(-0.5F, -5.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.1F)), PartPose.offset(2.5F, -1.0F, 0.0F));

		PartDefinition rightLeg = partdefinition.addOrReplaceChild("Right Leg", CubeListBuilder.create().texOffs(0, 8).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.1F)), PartPose.offset(0.95F, 6.0F, 0.0F));

		PartDefinition leftLeg = partdefinition.addOrReplaceChild("Left Leg", CubeListBuilder.create().texOffs(8, 24).addBox(-0.95F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 24).addBox(-0.95F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.1F)), PartPose.offset(-0.9F, 6.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	public void applyPose(String pose) {
		waist.setPos(0, 0, 0);
		waist.setRotation(0, 0, 0);

		head.setPos(0, 0, 0);
		head.setRotation(0, 0, 0);

		body.setPos(0, 0, 0);
		body.setRotation(0, 0, 0);

		rightArm.setPos(0, 0, 0);
		rightArm.setRotation(0, 0, 0);

		leftArm.setPos(0, 0, 0);
		leftArm.setRotation(0, 0, 0);

		rightLeg.setPos(0, 0, 0);
		rightLeg.setRotation(0, 0, 0);

		leftLeg.setPos(0, 0, 0);
		leftLeg.setRotation(0, 0, 0);

		switch (pose) {
			case "sitting" -> {
				rightLeg.xRot = (float) Math.toRadians(-90);
				rightLeg.yRot = (float) Math.toRadians(-22.5);
				rightLeg.setPos(1f, 0.5f, 0f);
				leftLeg.xRot = (float) Math.toRadians(-90);
				leftLeg.yRot = (float) Math.toRadians(22);
				leftLeg.setPos(-1f, 0.5f, 0f);
				waist.setPos(0f, 6.5f, 0f);
				rightArm.setPos(3.5f, 0, 0);
				//rightArm.zRot = (float) Math.toRadians(22.5);
				leftArm.setPos(-3.5f, 0, 0);
				//leftArm.zRot = (float) Math.toRadians(-22.5);
			}
			case "standing" -> {

			}
			case "waving" -> {

			}
			case "pointing" -> {

			}
			case "t_posing" -> {

			}
			case "jumping" -> {

			}
			case "balancing" -> {

			}
			case "jarona" -> {

			}
			case "fruit_bat" -> {

			}
			case "sad" -> {

			}
			case "crying" -> {

			}
			case "cool_sit" -> {

			}
			case "praying" -> {

			}
			case "laying" -> {

			}
			case "sliding" -> {

			}
			case "face_planting" -> {

			}
			case "floating" -> {

			}
			case "tape" -> {

			}
			case "hanging" -> {

			}
			case "horse" -> {

			}
		}
	}

	public ModelPart getWaist() {
		return this.waist;
	}

	public ModelPart getHead() {
		return this.head;
	}

	public ModelPart getBody() {
		return this.body;
	}

	public ModelPart getRightArm() {
		return this.rightArm;
	}

	public ModelPart getLeftArm() {
		return this.leftArm;
	}

	public ModelPart getRightLeg() {
		return this.rightLeg;
	}

	public ModelPart getLeftLeg() {
		return this.leftLeg;
	}
}