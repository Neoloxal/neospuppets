package com.neoloxal.neospuppets.puppets;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Puppet extends HorizontalDirectionalBlock {
    public static final MapCodec<Puppet> CODEC = simpleCodec(Puppet::new);

    public static final int MAX_POSE = 19;

    public static final IntegerProperty POSE = IntegerProperty.create("pose", 0, MAX_POSE);
    public static final EnumProperty<Skin> SKIN = EnumProperty.create("skin", Skin.class);

    public Puppet(Properties properties) {
        super(properties);
        this.registerDefaultState(getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POSE, 0)
                .setValue(SKIN, Skin.PUPPET)
        );
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POSE, SKIN);
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (!Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("tooltip.neospuppets.puppet"));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.neospuppets.puppet.shift_down.line_1"));
            tooltipComponents.add(Component.translatable("tooltip.neospuppets.puppet.shift_down.line_2"));
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        record SkinTransition(Skin fromSkin, Item item, Skin toSkin) {}

        final List<SkinTransition> SKIN_TRANSITIONS = List.of(
                new SkinTransition(Skin.PUPPET, Items.CYAN_WOOL, Skin.STEVE),
                new SkinTransition(Skin.PUPPET, Items.WHITE_WOOL, Skin.ALEX),
                new SkinTransition(Skin.PUPPET, Items.ORANGE_WOOL, Skin.ARI),
                new SkinTransition(Skin.PUPPET, Items.MAGENTA_WOOL, Skin.EFE),
                new SkinTransition(Skin.PUPPET, Items.PURPLE_WOOL, Skin.KAI),
                new SkinTransition(Skin.PUPPET, Items.YELLOW_WOOL, Skin.MAKENA),
                new SkinTransition(Skin.PUPPET, Items.GREEN_WOOL, Skin.NOOR),
                new SkinTransition(Skin.PUPPET, Items.LIME_WOOL, Skin.SUNNY),
                new SkinTransition(Skin.PUPPET, Items.RED_WOOL, Skin.ZURI)
        );

        final Map<Skin, Map<Item, Skin>> FORDWARD_MAP = SKIN_TRANSITIONS.stream()
                .collect(Collectors.groupingBy(
                        SkinTransition::fromSkin,
                        Collectors.toMap(SkinTransition::item, SkinTransition::toSkin)
                ));

        final Map<Skin, Item> REVERSE_MAP = SKIN_TRANSITIONS.stream()
                .collect(Collectors.toMap(SkinTransition::toSkin, SkinTransition::item));

        Item item = stack.getItem();
        Skin currentSkin = state.getValue(SKIN);

        if (!level.isClientSide()) {
            if (FORDWARD_MAP.containsKey(currentSkin)) {
                if (FORDWARD_MAP.get(currentSkin).containsKey(item)) {
                    if (currentSkin != FORDWARD_MAP.get(currentSkin).get(item)) {
                        level.setBlockAndUpdate(hitResult.getBlockPos(), state.setValue(SKIN, FORDWARD_MAP.get(currentSkin).get(item)));

                        if (!player.isCreative()) {
                            stack.shrink(1);
                        }

                        player.playNotifySound(SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1f, 1f);
                        return ItemInteractionResult.SUCCESS;
                    }
                }
            }
            if (item.asItem() == Items.SHEARS && hand == InteractionHand.MAIN_HAND) {
                if (!REVERSE_MAP.containsKey(currentSkin)) {
                    return ItemInteractionResult.FAIL;
                }

                level.setBlockAndUpdate(hitResult.getBlockPos(), state.setValue(SKIN, Skin.PUPPET));

                ItemStack itemStack = new ItemStack(REVERSE_MAP.get(currentSkin), 1);
                ItemEntity itemEntity = new ItemEntity(level, hitResult.getBlockPos().getX(), hitResult.getBlockPos().getY(), hitResult.getBlockPos().getZ(), itemStack);
                itemEntity.setDeltaMovement(0.0, 0.2, 0.0);

                if (!player.isCreative()) {
                    level.addFreshEntity(itemEntity);
                }

                player.playNotifySound(SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0f, 1.0f);
                player.getItemInHand(hand).hurtAndBreak(1, ((ServerLevel) level), player,
                        item1 -> player.onEquippedItemBroken(item1, EquipmentSlot.MAINHAND));

                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.FAIL;
        }

        if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() != Items.AIR) {
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.FAIL;
    }
}

