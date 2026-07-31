package com.neoloxal.neospuppets.puppets;

import com.mojang.authlib.GameProfile;
import com.neoloxal.neospuppets.NeosPuppets;
import net.minecraft.client.resources.PlayerSkin;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CustomPuppetBlockEntity extends BlockEntity {
    private String skinId = "0699057e-febf-47a0-9b16-552a5b64dd92";
    private Set<String> pendingFetches = new HashSet<>();
    private Map<String, ResourceLocation> cashedProfiles = new HashMap<>();

    private int pose = 0;

    public CustomPuppetBlockEntity(BlockPos pos, BlockState blockState) {
        super(NeosPuppets.CUSTOM_PUPPET_BLOCK_ENTITY.get(), pos, blockState);
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

    public boolean isFetchPending(String skinId) {
        return pendingFetches.contains(skinId);
    }

    public void markFetchPending(String skinId) {
        pendingFetches.add(skinId);
    }

    public void clearFetchPending(String skinId) {
        pendingFetches.remove(skinId);
    }

    public Map<String, ResourceLocation> getCashedProfiles() {
        return this.cashedProfiles;
    }

    public void casheProfile(String skinUUID, ResourceLocation skinTexture) {
        cashedProfiles.put(skinUUID, skinTexture);
        NeosPuppets.LOGGER.debug("Cashing " + skinUUID + " as " + skinTexture);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("pose", this.pose);
        tag.putString("skinId", this.skinId);
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        this.pose = tag.getInt("pose");
        this.skinId = tag.getString("skinId");
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
