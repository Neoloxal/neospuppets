package com.neoloxal.neospuppets.puppets;

import com.neoloxal.neospuppets.NeosPuppets;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CustomPuppetBlockEntity extends BlockEntity {
    private String skinId = "0699057e-febf-47a0-9b16-552a5b64dd92";

    private int pose = 0;
    private float xPos = 0f;
    private float yPos = 0f;
    private float zPos = 0f;

    public CustomPuppetBlockEntity(BlockPos pos, BlockState blockState) {
        super(NeosPuppets.CUSTOM_PUPPET_BLOCK_ENTITY.get(), pos, blockState);

        CompletableFuture.supplyAsync(() ->
                Minecraft.getInstance().getMinecraftSessionService().fetchProfile(UUID.fromString("0699057e-febf-47a0-9b16-552a5b64dd92"), false)
        ).thenCompose(result ->
                Minecraft.getInstance().getSkinManager().getOrLoad(result.profile())
        ).thenAccept(skin -> {
            Minecraft.getInstance().execute(() -> {
                NeosPuppets.casheProfile("default", skin.texture());
                NeosPuppets.clearFetchPending(skinId);
            });
        });
    }

    public float getXPos() {
        return xPos;
    }

    public void setXPos(float xPos) {
        this.xPos = xPos;

        this.setChanged();
        if (!getLevel().isClientSide()) {
            this.level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public float getYPos() {
        return yPos;
    }

    public void setYPos(float yPos) {
        this.yPos = yPos;

        this.setChanged();
        if (!getLevel().isClientSide()) {
            this.level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public float getZPos() {
        return zPos;
    }

    public void setZPos(float zPos) {
        this.zPos = zPos;

        this.setChanged();
        if (!getLevel().isClientSide()) {
            this.level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public int getPose() {
        return this.pose;
    }

    public void setPose(int pose) {
        this.pose = pose;

        this.setChanged();
        if (!getLevel().isClientSide()) {
            this.level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public String getSkinId() {
        return this.skinId;
    }

    public void setSkinId(String skinId) {
        this.skinId = skinId;

        this.setChanged();
        if (!getLevel().isClientSide()) {
            this.level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("pose", this.pose);

        tag.putString("skinId", this.skinId);

        tag.putFloat("xPos", this.xPos);
        tag.putFloat("yPos", this.yPos);
        tag.putFloat("zPos", this.zPos);

        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        this.pose = tag.getInt("pose");

        this.skinId = tag.getString("skinId");

        this.xPos = tag.getFloat("xPos");
        this.yPos = tag.getFloat("yPos");
        this.zPos = tag.getFloat("zPos");

        super.loadAdditional(tag, registries);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
