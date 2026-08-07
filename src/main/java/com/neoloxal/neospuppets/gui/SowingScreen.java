package com.neoloxal.neospuppets.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.neoloxal.neospuppets.NeosPuppets;
import com.neoloxal.neospuppets.client.PuppetModel;
import com.neoloxal.neospuppets.packets.SowingTextPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.CompletableFuture;

import static com.neoloxal.neospuppets.NeosPuppets.resolveProfile;

public class SowingScreen extends AbstractContainerScreen<SowingMenu> {
    public static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(NeosPuppets.MODID, "textures/gui/sowing_gui.png");

    private PuppetModel puppetModel;
    String skinId = "default";

    EditBox editBox;
    private String lastTypedText = "";
    private long lastEditTimeMs = 0;
    private static final long DEBOUNCE_DELAY_MS = 400;

    private ItemStack lastInputStack = ItemStack.EMPTY;

    public SowingScreen(SowingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();

        editBox = new EditBox(this.font, this.leftPos + 53, this.topPos + 15, 74, 16, Component.translatable("menu.neospuppets.sowing.edit_box")) {
            @Override
            public void setFocused(boolean focused) {
                boolean wasFocused = this.isFocused();
                super.setFocused(focused);
                if (wasFocused && !focused) {
                    submitText(this.getValue());
                }
            }
        };
        editBox.setMaxLength(16);
        editBox.setHint(Component.translatable("menu.neospuppets.sowing.edit_box.hint"));
        editBox.setCanLoseFocus(true);
        editBox.setResponder(text -> lastEditTimeMs = System.currentTimeMillis());

        this.addRenderableWidget(editBox);

        this.puppetModel = new PuppetModel(Minecraft.getInstance().getEntityModels().bakeLayer(PuppetModel.LAYER_LOCATION));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 176, 166);

        PoseStack poseStack = guiGraphics.pose();

        ResourceLocation skinTexture = NeosPuppets.getCashedProfiles().get(skinId);

        puppetModel.applyPose("sitting");

        //NeosPuppets.LOGGER.debug("About to render with texture " + skinTexture);
        VertexConsumer buffer;
        if (skinTexture == null) {
            skinTexture = NeosPuppets.getCashedProfiles().get("default");
        }

        if (skinTexture == null) {
            return; // Safety fallback in case default hasn't initialized
        }

        buffer = guiGraphics.bufferSource().getBuffer(RenderType.entityCutout(skinTexture));

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();

        poseStack.pushPose();
        poseStack.translate(x + imageWidth / 2.0 - 24, y - 16, 100);
        poseStack.scale(48, 48, 48);

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.XN.rotationDegrees(45));
        poseStack.mulPose(Axis.YP.rotationDegrees(135));
        poseStack.translate(-0.5, -0.5, -0.5);
        poseStack.translate(0.5, 1, 0.5);

        poseStack.pushPose();
        poseStack.scale(-1, 1, 1);
        puppetModel.getWaist().render(poseStack, buffer, LightTexture.pack(0, 15), OverlayTexture.NO_OVERLAY);
        puppetModel.getLeftLeg().render(poseStack, buffer, LightTexture.pack(0, 15), OverlayTexture.NO_OVERLAY);
        puppetModel.getRightLeg().render(poseStack, buffer, LightTexture.pack(0, 15), OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
        poseStack.popPose();
        guiGraphics.flush();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.editBox.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                submitText(this.editBox.getValue());
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_E) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (editBox.isFocused() && !editBox.isMouseOver(mouseX, mouseY)) {
            editBox.setFocused(false);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        String currentText = editBox.getValue();
        boolean pastDebounceWindow = System.currentTimeMillis() - lastEditTimeMs > DEBOUNCE_DELAY_MS;
        if (pastDebounceWindow && !currentText.equals(lastTypedText) && lastEditTimeMs != 0) {
            lastTypedText = currentText;
            submitText(currentText);
        }

        ItemStack currentInputStack = this.menu.getSlot(36).getItem();
        if (!ItemStack.matches(currentInputStack, lastInputStack)) {
            lastInputStack = currentInputStack.copy();
            if (!currentText.isEmpty()) {
                submitText(currentText);
            }
        }
    }

    private void submitText(String text) {
        PacketDistributor.sendToServer(new SowingTextPacket(text));
        downloadSkin(text);
    }

    private void downloadSkin(String skinName) {
        CompletableFuture.supplyAsync(() -> resolveProfile(skinName))
                .thenAccept(uuidOptional -> {
                    uuidOptional.ifPresentOrElse(uuid -> {
                        String skinUUID = uuid.toString();

                        if (NeosPuppets.isFetchPending(skinUUID)) {
                            return;
                        }
                        NeosPuppets.markFetchPending(skinUUID);

                        CompletableFuture.supplyAsync(() ->
                                Minecraft.getInstance().getMinecraftSessionService().fetchProfile(uuid, false)
                        ).thenCompose(result ->
                                Minecraft.getInstance().getSkinManager().getOrLoad(result.profile())
                        ).thenAccept(skin -> Minecraft.getInstance().execute(() -> {
                            NeosPuppets.casheProfile(skinUUID, skin.texture());
                            NeosPuppets.clearFetchPending(skinUUID);
                            skinId = skinUUID;
                        }));
                    }, () -> {
                        NeosPuppets.LOGGER.debug("No account found for name: " + skinName);
                    });
                });
    }
}
