package com.neoloxal.neospuppets.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Compact player-skin puppet model.
 *
 * Geometry uses the normal entity-model Y-down coordinate system. The block
 * entity renderer flips X/Y before drawing, matching Minecraft's player model
 * UV orientation and preventing vertically inverted faces.
 */
public class PuppetModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("neospuppets", "puppetmodel"), "main");

    private final ModelPart waist;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

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
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition waist = root.addOrReplaceChild("Waist", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        waist.addOrReplaceChild("Head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 0).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.10F)),
                PartPose.offset(0.0F, 4.0F, 0.0F));

        waist.addOrReplaceChild("Body",
                CubeListBuilder.create()
                        .texOffs(8, 8).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(8, 16).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.10F)),
                PartPose.offset(0.0F, 4.0F, 0.0F));

        waist.addOrReplaceChild("Right Arm",
                CubeListBuilder.create()
                        .texOffs(20, 8).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(20, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.10F)),
                PartPose.offset(-3.0F, 4.0F, 0.0F));

        waist.addOrReplaceChild("Left Arm",
                CubeListBuilder.create()
                        .texOffs(16, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.10F)),
                PartPose.offset(3.0F, 4.0F, 0.0F));

        root.addOrReplaceChild("Right Leg",
                CubeListBuilder.create()
                        .texOffs(0, 8).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.10F)),
                PartPose.offset(-1.0F, 10.0F, 0.0F));

        root.addOrReplaceChild("Left Leg",
                CubeListBuilder.create()
                        .texOffs(8, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.10F)),
                PartPose.offset(1.0F, 10.0F, 0.0F));

        // Half-size geometry with half-size UV coordinates preserves the exact
        // normalized regions of a 64x64 player skin.
        return LayerDefinition.create(mesh, 32, 32);
    }

    public void applyPose(String pose) {
        resetStandingPose();
        if ("sitting".equals(pose)) {
            this.waist.setPos(0.0F, 5.0F, 0.0F);

            this.rightLeg.setPos(-1.0F, 14.0F, 0.0F);
            this.rightLeg.xRot = radians(-90.0F);
            this.rightLeg.yRot = radians(-22.5F);

            this.leftLeg.setPos(1.0F, 14.0F, 0.0F);
            this.leftLeg.xRot = radians(-90.0F);
            this.leftLeg.yRot = radians(22.5F);
        }
    }

    private void resetStandingPose() {
        this.waist.setPos(0.0F, 0.0F, 0.0F);
        setRotation(this.waist, 0.0F, 0.0F, 0.0F);

        this.head.setPos(0.0F, 4.0F, 0.0F);
        setRotation(this.head, 0.0F, 0.0F, 0.0F);

        this.body.setPos(0.0F, 4.0F, 0.0F);
        setRotation(this.body, 0.0F, 0.0F, 0.0F);

        this.rightArm.setPos(-3.0F, 4.0F, 0.0F);
        setRotation(this.rightArm, 0.0F, 0.0F, 0.0F);

        this.leftArm.setPos(3.0F, 4.0F, 0.0F);
        setRotation(this.leftArm, 0.0F, 0.0F, 0.0F);

        this.rightLeg.setPos(-1.0F, 10.0F, 0.0F);
        setRotation(this.rightLeg, 0.0F, 0.0F, 0.0F);

        this.leftLeg.setPos(1.0F, 10.0F, 0.0F);
        setRotation(this.leftLeg, 0.0F, 0.0F, 0.0F);
    }

    private static void setRotation(ModelPart part, float x, float y, float z) {
        part.xRot = x;
        part.yRot = y;
        part.zRot = z;
    }

    private static float radians(float degrees) {
        return (float) Math.toRadians(degrees);
    }

    public ModelPart getWaist() { return this.waist; }
    public ModelPart getHead() { return this.head; }
    public ModelPart getBody() { return this.body; }
    public ModelPart getRightArm() { return this.rightArm; }
    public ModelPart getLeftArm() { return this.leftArm; }
    public ModelPart getRightLeg() { return this.rightLeg; }
    public ModelPart getLeftLeg() { return this.leftLeg; }
}
