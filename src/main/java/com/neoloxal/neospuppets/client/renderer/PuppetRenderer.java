package com.neoloxal.neospuppets.client.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.neoloxal.neospuppets.NeosPuppets;
import com.neoloxal.neospuppets.client.PuppetModel;
import com.neoloxal.neospuppets.puppets.CustomPuppetBlock;
import com.neoloxal.neospuppets.puppets.CustomPuppetBlockEntity;
import com.neoloxal.neospuppets.puppets.Puppet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PuppetRenderer implements BlockEntityRenderer<CustomPuppetBlockEntity> {
    private final PuppetModel puppetModel;

    public PuppetRenderer(BlockEntityRendererProvider.Context context) {
        this.puppetModel = new PuppetModel(context.bakeLayer(PuppetModel.LAYER_LOCATION));
    }

    @Override
    public void render(CustomPuppetBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        String skinId = blockEntity.getSkinId();

        ResourceLocation skinTexture = blockEntity.getCashedProfiles().get(skinId);
        if (skinTexture == null) {
            if (!blockEntity.isFetchPending(skinId)) {
                GameProfile profile;
                //profile = new GameProfile(UUID.fromString("0699057e-febf-47a0-9b16-552a5b64dd92"), "");
                try {
                    profile = new GameProfile(UUID.fromString(blockEntity.getSkinId()), "Neoloxal");
                    CompletableFuture.supplyAsync(() ->
                            Minecraft.getInstance().getMinecraftSessionService().fetchProfile(UUID.fromString(skinId), false)
                    ).thenCompose(result ->
                            Minecraft.getInstance().getSkinManager().getOrLoad(result.profile())
                    ).thenAccept(skin -> {
                            Minecraft.getInstance().execute(() -> {
                                blockEntity.casheProfile(skinId, skin.texture());
                                blockEntity.clearFetchPending(skinId);
                            });
                        });
                    blockEntity.markFetchPending(skinId);
                } catch (IllegalArgumentException exception) {
                    skinTexture = blockEntity.getCashedProfiles().get("default");
                }
            }
            skinTexture = blockEntity.getCashedProfiles().get("default");
        }

        String currentPose = Puppet.POSES.get(blockEntity.getPose());
        puppetModel.applyPose(currentPose);

        //NeosPuppets.LOGGER.debug("About to render with texture " + skinTexture);
        VertexConsumer buffer;
        if (skinTexture != null) {
            buffer = bufferSource.getBuffer(RenderType.entityCutout(skinTexture));
        } else {
            return;
        }

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
        poseStack.translate(0.5, 0, 0.5);
        poseStack.translate(blockEntity.getXPos(), blockEntity.getYPos(), blockEntity.getZPos());

        puppetModel.getWaist().render(poseStack, buffer, packedLight, packedOverlay);
        puppetModel.getLeftLeg().render(poseStack, buffer, packedLight, packedOverlay);
        puppetModel.getRightLeg().render(poseStack, buffer, packedLight, packedOverlay);

        poseStack.popPose();
    }
}
