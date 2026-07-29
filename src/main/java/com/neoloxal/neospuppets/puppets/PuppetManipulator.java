package com.neoloxal.neospuppets.puppets;

import com.neoloxal.neospuppets.NeosPuppets;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PuppetManipulator extends Item {
    public PuppetManipulator(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        Block clickedBlock = level.getBlockState(context.getClickedPos()).getBlock();
        Player player = context.getPlayer();

        if (!level.isClientSide()) {
            if (clickedBlock.equals(NeosPuppets.PUPPET.get())) {
                if (context.getHand() == InteractionHand.MAIN_HAND) {
                    int currentPose = state.getValue(Puppet.POSE);
                    int poseDirection = 1;
                    if (player.isShiftKeyDown()) {
                        poseDirection = -1;
                    }

                    int newPose = (currentPose + (poseDirection) + (Puppet.MAX_POSE + 1)) % (Puppet.MAX_POSE + 1);
                    level.setBlockAndUpdate(context.getClickedPos(), state.setValue(Puppet.POSE, newPose));

                    context.getItemInHand().hurtAndBreak(1, ((ServerLevel) level), player,
                            item -> player.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));

                    player.displayClientMessage(Component.translatable("messages.neospuppets.puppet_pose_changed", (newPose + 1), (Puppet.MAX_POSE + 1)), true);
                    player.playNotifySound(SoundEvents.WOODEN_BUTTON_CLICK_ON, SoundSource.BLOCKS, 1f, 1f);
                    return InteractionResult.SUCCESS;
                } else {
                    Direction currentDirection = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                    Direction newDirection = currentDirection.getClockWise();
                    if (player.isShiftKeyDown()) {
                        newDirection = currentDirection.getCounterClockWise();
                    }

                    level.setBlockAndUpdate(context.getClickedPos(), state.setValue(BlockStateProperties.HORIZONTAL_FACING, newDirection));

                    context.getItemInHand().hurtAndBreak(1, ((ServerLevel) level), player,
                            item -> player.onEquippedItemBroken(item, EquipmentSlot.OFFHAND));

                    player.playNotifySound(SoundEvents.WOODEN_BUTTON_CLICK_ON, SoundSource.BLOCKS, 1f, 1f);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.FAIL;
    }
}
