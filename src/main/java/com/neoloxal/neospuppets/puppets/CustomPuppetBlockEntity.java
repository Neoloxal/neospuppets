package com.neoloxal.neospuppets.puppets;

import com.neoloxal.neospuppets.NeosPuppets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CustomPuppetBlockEntity extends BlockEntity {
    private int pose = 0;

    public CustomPuppetBlockEntity(BlockPos pos, BlockState blockState) {
        super(NeosPuppets.CUSTOM_PUPPET_BLOCK_ENTITY.get(), pos, blockState);
    }

    public int getPose() {
        return this.pose;
    }

    public void setPose(int pose) {
        this.pose = pose;
        setChanged();
        if (!getLevel().isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("pose", this.pose);
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        this.pose = tag.getInt("pose");
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
