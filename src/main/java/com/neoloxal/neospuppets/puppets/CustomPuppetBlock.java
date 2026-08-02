package com.neoloxal.neospuppets.puppets;

import com.mojang.authlib.GameProfile;
import com.neoloxal.neospuppets.NeosPuppets;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CustomPuppetBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING  = BlockStateProperties.HORIZONTAL_FACING;

    public CustomPuppetBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(getStateDefinition().any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CustomPuppetBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection();
        if (!context.getPlayer().isShiftKeyDown()) {
            direction = direction.getOpposite();
        }
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, direction);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof CustomPuppetBlockEntity blockEntity) {
                NeosPuppets.decasheProfile(blockEntity.getSkinId());
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() == Items.SHEARS) {
            if (!level.isClientSide()) {
                CustomPuppetBlockEntity blockEntity = (CustomPuppetBlockEntity) level.getBlockEntity(pos);

                level.setBlockAndUpdate(pos, NeosPuppets.PUPPET.get().defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING))
                        .setValue(Puppet.POSE, blockEntity.getPose())
                        .setValue(Puppet.SKIN, Skin.PUPPET)
                );

                player.getItemInHand(hand).hurtAndBreak(1, ((ServerLevel) level), player,
                        item -> player.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));

                String skinId = blockEntity.getSkinId();

                CompletableFuture.supplyAsync(() ->
                        level.getServer().getSessionService().fetchProfile(UUID.fromString(skinId), false)
                ).thenAccept(profileResult -> {
                            String skinName = profileResult.profile().getName();
                            level.getServer().execute(() -> {
                                ItemStack itemStack = new ItemStack(NeosPuppets.PATTERN_FABRIC.get());
                                itemStack.set(NeosPuppets.SKIN_COMPONENT, new NeosPuppets.skinRecord(skinId, skinName));
                                ItemEntity itemEntity = new ItemEntity(level, hitResult.getBlockPos().getX(), hitResult.getBlockPos().getY(), hitResult.getBlockPos().getZ(), itemStack);
                                itemEntity.setDeltaMovement(0.0, 0.2, 0.0);

                                if (!player.isCreative()) {
                                    level.addFreshEntity(itemEntity);
                                }
                            });
                        });

                player.playNotifySound(SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1f, 1f);
            }
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.FAIL;
    }
}
