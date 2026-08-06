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
import org.joml.Vector3f;

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
                PartPose.offset(-2.5F, 10.0F, 0.0F));

        waist.addOrReplaceChild("Left Arm",
                CubeListBuilder.create()
                        .texOffs(16, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.10F)),
                PartPose.offset(2.5F, 10.0F, 0.0F));

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
        if (pose.equals("sitting")) {
            this.waist.setPos(0.0F, 6.0F, 0.0F);

            this.rightLeg.setPos(-1.0F, 15.0F, 0.0F);
            this.rightLeg.xRot = radians(-90.0F);
            this.rightLeg.yRot = radians(22.5F);

            this.leftLeg.setPos(1.0F, 15.0F, 0.0F);
            this.leftLeg.xRot = radians(-90.0F);
            this.leftLeg.yRot = radians(-22.5F);
        }

        if (pose.equals("standing")) {}

        if (pose.equals("waving")) {
            this.rightArm.zRot = radians(22.5f);
            this.leftArm.zRot = radians(-156.25f);

            this.leftArm.setPos(1.5F, 3.0F, 0.0F);
            this.leftArm.offsetPos(new Vector3f(1.5F, 3.0F, 0.0F));
        }

        if (pose.equals("pointing")) {
            this.rightArm.xRot = radians(-90);
        }

        if (pose.equals("t_posing")) {
            this.rightArm.zRot = radians(90);
            this.rightArm.setPos(-2.0F, 4.0F, 0.0F);
            this.leftArm.zRot = radians(-90);
            this.leftArm.setPos(2.0F, 4.0F, 0.0F);
        }

        if (pose.equals("jumping")) {
            this.waist.setPos(0, -2.5f, 0);

            this.leftArm.xRot = radians(-67.5f);
            this.rightArm.xRot = radians(67.5f);

            this.leftLeg.xRot = radians(45f);
            this.leftLeg.setPos(1.0F, 7.5F, 0.0F);
            this.rightLeg.xRot = radians(-45f);
            this.rightLeg.setPos(-1.0F, 7.5F, 0.0F);
        }

        if (pose.equals("balancing")) {
            this.waist.zRot = radians(-45f);
            this.waist.setPos(-7.5f,2f,0);

            this.leftLeg.zRot = radians(-67.5f);
            this.leftLeg.setPos(.5F, 8.5F, 0.0F);

            this.rightArm.zRot = radians(105);
            this.rightArm.setPos(-2.0F, 4.0F, 0.0F);

            this.leftArm.zRot = radians(-100);
            this.leftArm.setPos(2.0F, 4.0F, 0.0F);
        }

        if (pose.equals("jarona")) {
            this.head.yRot = radians(22.5f);
            this.body.xRot = radians(-22.5f);

            this.rightArm.zRot = radians(-22.5f);
            this.rightArm.setPos(-4.5F, 4.0F, 0.0F);

            this.leftArm.xRot = radians(-225.5f);

            this.rightLeg.xRot = radians(45f);
            this.rightLeg.setPos(-1.0F, 10.0F, -2.0F);
            this.leftLeg.xRot = radians(22.5f);
        }

        if (pose.equals("fruit_bat")) {
            this.waist.zRot = radians(180f);
            this.waist.setPos(0.0F, 16F, 0.0F);

            this.leftLeg.zRot = radians(180f);
            this.leftLeg.setPos(-1.0F, 6F, 0.0F);
            this.rightLeg.zRot = radians(180f);
            this.rightLeg.setPos(1.0F, 6F, 0.0F);
        }

        if (pose.equals("sad")) {
            leftArm.xRot = radians(180f);
            leftArm.setPos(1.5F, 6.0F, -2.5F);
            rightArm.xRot = radians(180f);
            rightArm.setPos(-1.5F, 6.0F, -2.5F);
        }

        if (pose.equals("crying")) {
            this.waist.setPos(0, 6.25f, 3.5f);
            this.head.xRot = radians(22.5f);
            this.head.setPos(0.0F, 3.75F, 0.0F);

            this.leftArm.xRot = radians(-90f);
            this.leftArm.yRot = radians(15f);

            this.rightArm.xRot = radians(-90f);
            this.rightArm.yRot = radians(-15f);

            this.leftLeg.setPos(1.25F, 10.0F, 0.0F);
            this.rightLeg.setPos(-1.25F, 10.0F, 0.0F);
        }

        if (pose.equals("cool_sit")) {
            this.waist.setPos(0, 6.25f, 3.5f);
            this.head.xRot = radians(22.5f);
            this.head.setPos(0.0F, 4F, 0.0F);

            this.leftArm.zRot = radians(-22.5f);

            this.rightArm.xRot = radians(-90f);
            this.rightArm.yRot = radians(-15f);

            this.leftLeg.xRot = radians(-90f);
            this.leftLeg.setPos(1F, 15.0F, 3F);
            this.rightLeg.setPos(-1.25F, 10.0F, 0.0F);
        }

        if (pose.equals("praying")) {
            this.waist.setPos(0, 5, 0);

            this.leftArm.xRot = radians(-90f);
            this.leftArm.yRot = radians(22.5f);
            this.leftArm.setPos(3.0F, 5.0F, 0.0f);
            this.rightArm.xRot = radians(-90f);
            this.rightArm.yRot = radians(-22.5f);
            this.rightArm.setPos(-3.0F, 5.0F, 0.0f);

            this.leftLeg.xRot = radians(90);
            this.leftLeg.setPos(1F, 15.0F, 0F);
            this.rightLeg.xRot = radians(90);
            this.rightLeg.setPos(-1F, 15.0F, 0F);
        }

        if (pose.equals("laying")) {
            this.waist.xRot = radians(-90);
            this.waist.setPos(0, 15, 8);

            this.rightLeg.xRot = radians(-90);
            this.rightLeg.setPos(-1.0F, 15.0F, -2.0F);
            this.leftLeg.xRot = radians(-90);
            this.leftLeg.setPos(1.0F, 15.0F, -2.0F);
        }

        if (pose.equals("sliding")) {
            this.waist.xRot = radians(-67.5f);
            this.waist.setPos(0, 11, 7);

            this.head.xRot = radians(22.5f);

            this.rightArm.xRot = radians(135f);
            this.rightArm.setPos(-3.0F, 5.0F, 0.0F);
            this.leftArm.xRot = radians(-67.5f);

            this.rightLeg.xRot = radians(-90);
            this.rightLeg.setPos(-1.0F, 15.0F, -2.0F);
            this.leftLeg.xRot = radians(-67.5f);
            this.leftLeg.setPos(1.0F, 13.0F, -2.0F);
        }

        if (pose.equals("face_planting")) {
            this.waist.xRot = radians(-90);
            this.waist.zRot = radians(180);
            this.waist.setPos(0, 15, 8);

            this.rightArm.zRot = radians(135f);
            this.rightArm.setPos(-2.0F, 5.0F, 0.0F);
            this.leftArm.zRot = radians(-135f);
            this.leftArm.setPos(2.0F, 5.0F, 0.0F);

            this.rightLeg.xRot = radians(-90);
            this.rightLeg.zRot = radians(180);
            this.rightLeg.yRot = radians(22.5f);
            this.rightLeg.setPos(1.0F, 15.0F, -2.0F);
            this.leftLeg.xRot = radians(-90);
            this.leftLeg.zRot = radians(180);
            this.leftLeg.yRot = radians(-22.5f);
            this.leftLeg.setPos(-1.0F, 15.0F, -2.0F);
        }

        if (pose.equals("floating")) {
            this.waist.xRot = radians(-90);
            this.waist.setPos(0, 8, 8);

            this.rightArm.zRot = radians(90);
            this.rightArm.xRot = radians(45);
            this.rightArm.setPos(-2.0F, 4.0F, 0.0F);
            this.leftArm.zRot = radians(-90);
            this.leftArm.xRot = radians(45);
            this.leftArm.setPos(2.0F, 4.0F, 0.0F);

            this.rightLeg.xRot = radians(-45);
            this.rightLeg.setPos(-1.0F, 8.0F, -2.0F);
            this.leftLeg.xRot = radians(-67.5f);
            this.leftLeg.setPos(1.0F, 8.0F, -2.0F);
        }

        if (pose.equals("tape")) {
            this.waist.yRot = radians(180);
            this.waist.setPos(0, 0, 7f);

            this.rightArm.zRot = radians(135f);
            this.rightArm.setPos(-2.0F, 5.0F, 0.0F);
            this.leftArm.zRot = radians(-135f);
            this.leftArm.setPos(2.0F, 5.0F, 0.0F);

            this.rightLeg.yRot = radians(180);
            this.rightLeg.zRot = radians(-22.5f);
            this.rightLeg.setPos(1.0F, 10.0F, 7f);
            this.leftLeg.yRot = radians(180);
            this.leftLeg.zRot = radians(22.5f);
            this.leftLeg.setPos(-1.0F, 10.0F, 7f);
        }

        if (pose.equals("hanging")) {
            this.waist.yRot = radians(180);
            this.waist.setPos(0, 0, 7f);

            this.rightArm.xRot = radians(-165f);
            this.rightArm.setPos(-3.0F, 5.0F, 0.0F);
            this.leftArm.xRot = radians(-165f);
            this.leftArm.setPos(3.0F, 5.0F, 0.0F);

            this.rightLeg.yRot = radians(180);
            this.rightLeg.setPos(1.0F, 10.0F, 7f);
            this.leftLeg.yRot = radians(180);
            this.leftLeg.setPos(-1.0F, 10.0F, 7f);
        }

        if (pose.equals("horse")) {
            this.waist.xRot = radians(90);
            this.waist.setPos(0, 10, -6);

            this.head.xRot = radians(-90);
            this.head.setPos(0.0F, 4.0F, 2.0F);

            this.body.setPos(0.0F, 4.0F, 1.0F);

            this.rightArm.xRot = radians(-90);
            this.rightArm.setPos(-1.0F, 4.0F, 0.0F);
            this.leftArm.xRot = radians(-90);
            this.leftArm.setPos(1.0F, 4.0F, 0.0F);

            this.rightLeg.setPos(-1.0F, 10.0F, 4.0F);
            this.leftLeg.setPos(1.0F, 10.0F, 4.0F);
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
