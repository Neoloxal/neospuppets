package com.neoloxal.neospuppets.items;

import com.neoloxal.neospuppets.NeosPuppets;
import com.neoloxal.neospuppets.blocks.puppets.CustomPuppetBlockEntity;
import com.neoloxal.neospuppets.blocks.puppets.Puppet;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

@EventBusSubscriber(modid = NeosPuppets.MODID)
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
        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
        CustomPuppetBlockEntity puppetEntity = (CustomPuppetBlockEntity) blockEntity;
        boolean isBlockEntity = false;

        if (!level.isClientSide()) {
            if (clickedBlock.equals(NeosPuppets.PUPPET.get()) || clickedBlock.equals(NeosPuppets.CUSTOM_PUPPET_BLOCK.get())) {
                if (clickedBlock.equals(NeosPuppets.CUSTOM_PUPPET_BLOCK.get())) {
                    isBlockEntity = true;
                }
                if (context.getHand() == InteractionHand.MAIN_HAND) {
                    int currentPose;
                    if (!isBlockEntity) {
                        currentPose = state.getValue(Puppet.POSE);
                    } else {
                        currentPose = puppetEntity.getPose();
                    }
                    int poseDirection = 1;
                    if (player.isShiftKeyDown()) {
                        poseDirection = -1;
                    }

                    int newPose = (currentPose + (poseDirection) + (Puppet.MAX_POSE + 1)) % (Puppet.MAX_POSE + 1);
                    if (!isBlockEntity) {
                        level.setBlockAndUpdate(context.getClickedPos(), state.setValue(Puppet.POSE, newPose));
                    } else {
                        puppetEntity.setPose(newPose);
                    }

                    context.getItemInHand().hurtAndBreak(1, ((ServerLevel) level), player,
                            item -> player.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));

                    player.displayClientMessage(Component.translatable("message.neospuppets.puppet_manipulator.pose_changed", (newPose + 1), (Puppet.MAX_POSE + 1)), true);
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

    @SubscribeEvent
    public static void clipboardEvents(PlayerInteractEvent.LeftClickBlock context) {
        Level level = context.getLevel();

        BlockPos pos = context.getPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        Player player = context.getEntity();

        ItemStack stack = player.getItemInHand(context.getHand());

        if (stack.is(NeosPuppets.PUPPET_MANIPULATOR)) {
            boolean isBlockEntity = false;
            CustomPuppetBlockEntity puppetEntity = null;
            if (level.getBlockEntity(pos) instanceof CustomPuppetBlockEntity) {
                puppetEntity = (CustomPuppetBlockEntity) level.getBlockEntity(pos);
                if (puppetEntity != null) {isBlockEntity = true;}
            } else if (!(block instanceof Puppet)) {
                return;
            }

            if (block == NeosPuppets.PUPPET.get() || block == NeosPuppets.CUSTOM_PUPPET_BLOCK.get()) {
                if (!level.isClientSide()) {
                    if (isBlockEntity) {
                        if (player.isShiftKeyDown()) {
                            boolean firstUse = false;
                            boolean displayMessage = false;
                            if (!stack.has(NeosPuppets.POSE_CLIPBOARD_COMPONENT)) {firstUse = true;}
                            if (firstUse || puppetEntity.getPose() != stack.get(NeosPuppets.POSE_CLIPBOARD_COMPONENT)) {
                                displayMessage = true;
                            }

                            stack.set(NeosPuppets.POSE_CLIPBOARD_COMPONENT, puppetEntity.getPose());
                            NeosPuppets.LOGGER.debug("Copying pose " + puppetEntity.getPose() + " to clipboard.");
                            if (displayMessage) {
                                player.displayClientMessage(Component.translatable("message.neospuppets.puppet_manipulator.clipboard_copy", (stack.get(NeosPuppets.POSE_CLIPBOARD_COMPONENT) + 1)), true);
                                player.playNotifySound(SoundEvents.CHERRY_WOOD_DOOR_OPEN, SoundSource.BLOCKS, 1f, .8f);
                            }
                        } else {
                            if (stack.has(NeosPuppets.POSE_CLIPBOARD_COMPONENT)) {
                                if (puppetEntity.getPose() != stack.get(NeosPuppets.POSE_CLIPBOARD_COMPONENT)) {
                                    puppetEntity.setPose(stack.get(NeosPuppets.POSE_CLIPBOARD_COMPONENT));
                                    stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                                    NeosPuppets.LOGGER.debug("Pasting pose " + stack.get(NeosPuppets.POSE_CLIPBOARD_COMPONENT) + ".");
                                    player.displayClientMessage(Component.translatable("message.neospuppets.puppet_manipulator.clipboard_paste", (stack.get(NeosPuppets.POSE_CLIPBOARD_COMPONENT) + 1)), true);
                                    player.playNotifySound(SoundEvents.CHERRY_WOOD_DOOR_OPEN, SoundSource.BLOCKS, 1f, .75f);
                                }
                            }
                        }
                    } else {
                        if (player.isShiftKeyDown()) {
                            boolean firstUse = false;
                            boolean displayMessage = false;
                            if (!stack.has(NeosPuppets.POSE_CLIPBOARD_COMPONENT)) {firstUse = true;}
                            if (firstUse || !state.getValue(Puppet.POSE).equals(stack.get(NeosPuppets.POSE_CLIPBOARD_COMPONENT))) {
                                displayMessage = true;
                            }

                            stack.set(NeosPuppets.POSE_CLIPBOARD_COMPONENT, state.getValue(Puppet.POSE));
                            NeosPuppets.LOGGER.debug("Copying pose " + state.getValue(Puppet.POSE) + " to clipboard.");
                            if (displayMessage) {
                                player.displayClientMessage(Component.translatable("message.neospuppets.puppet_manipulator.clipboard_copy", (stack.get(NeosPuppets.POSE_CLIPBOARD_COMPONENT) + 1)), true);
                                player.playNotifySound(SoundEvents.CHERRY_WOOD_DOOR_OPEN, SoundSource.BLOCKS, 1f, .8f);
                            }
                        } else {
                            if (stack.has(NeosPuppets.POSE_CLIPBOARD_COMPONENT)) {
                                if (!state.getValue(Puppet.POSE).equals(stack.get(NeosPuppets.POSE_CLIPBOARD_COMPONENT))) {
                                    level.setBlockAndUpdate(pos, state.setValue(Puppet.POSE, stack.get(NeosPuppets.POSE_CLIPBOARD_COMPONENT)));
                                    stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                                    NeosPuppets.LOGGER.debug("Pasting pose " + stack.get(NeosPuppets.POSE_CLIPBOARD_COMPONENT) + ".");
                                    player.displayClientMessage(Component.translatable("message.neospuppets.puppet_manipulator.clipboard_paste", (stack.get(NeosPuppets.POSE_CLIPBOARD_COMPONENT) + 1)), true);
                                    player.playNotifySound(SoundEvents.CHERRY_WOOD_DOOR_OPEN, SoundSource.BLOCKS, 1f, .75f);
                                }
                            }
                        }
                    }
                }
                context.setCanceled(true);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (!Screen.hasControlDown()) {
            tooltipComponents.add(Component.translatable("tooltip.neospuppets.puppet_manipulator"));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.neospuppets.puppet_manipulator.control_down.line_1"));
            tooltipComponents.add(Component.translatable("tooltip.neospuppets.puppet_manipulator.control_down.line_2"));
        }

        if (stack.has(NeosPuppets.POSE_CLIPBOARD_COMPONENT)) {
            tooltipComponents.add(Component.translatable("tooltip.neospuppets.puppet_manipulator.clipboard", (stack.get(NeosPuppets.POSE_CLIPBOARD_COMPONENT) + 1)));
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
