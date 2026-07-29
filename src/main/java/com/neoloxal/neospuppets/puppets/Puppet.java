package com.neoloxal.neospuppets.puppets;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
}

