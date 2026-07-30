package com.neoloxal.neospuppets.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.neoloxal.neospuppets.NeosPuppets;
import com.neoloxal.neospuppets.puppets.CustomPuppetBlock;
import com.neoloxal.neospuppets.puppets.CustomPuppetBlockEntity;
import com.neoloxal.neospuppets.puppets.Puppet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.data.ModelData;

public class PuppetRenderer implements BlockEntityRenderer<CustomPuppetBlockEntity> {
    public PuppetRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CustomPuppetBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        String currentPose = Puppet.POSES.get(blockEntity.getPose());
        ModelResourceLocation model = ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath("neospuppets", "puppet/generic/" + currentPose));
        BakedModel bakedModel = Minecraft.getInstance().getModelManager().getModel(model);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutout());
        ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();


        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        Direction facing = blockEntity.getBlockState().getValue(CustomPuppetBlock.FACING);
        float rotationDegrees = switch (facing) {
            case NORTH -> 0f;
            case EAST -> 270f;
            case SOUTH -> 180f;
            case WEST -> 90f;
            default -> 0f;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationDegrees));
        poseStack.translate(-0.5, -0.5, -0.5);

        renderer.renderModel(poseStack.last() , buffer, blockEntity.getBlockState(), bakedModel,
                1.0f, 1.0f, 1.0f, packedLight, packedOverlay, ModelData.EMPTY, RenderType.cutout());

        poseStack.popPose();
    }
}
