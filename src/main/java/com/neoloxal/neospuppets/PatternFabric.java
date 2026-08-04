package com.neoloxal.neospuppets;

import com.mojang.authlib.GameProfile;
import com.neoloxal.neospuppets.puppets.CustomPuppetBlockEntity;
import com.neoloxal.neospuppets.puppets.Puppet;
import com.neoloxal.neospuppets.puppets.Skin;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PatternFabric extends Item {
    public PatternFabric(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (stack.has(NeosPuppets.SKIN_COMPONENT)) {
            tooltipComponents.add(Component.translatable("tooltip.neospuppets.pattern_fabric.bound", stack.get(NeosPuppets.SKIN_COMPONENT)));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.neospuppets.pattern_fabric"));
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    private static Optional<GameProfile> resolveProfile(MinecraftServer server, String name) {
        ServerPlayer online = server.getPlayerList().getPlayerByName(name);
        if (online != null) {
            return Optional.of(online.getGameProfile());
        }

        Optional<GameProfile> cached = server.getProfileCache().get(name);
        if (cached.isPresent()) {
            return cached;
        }

        UUID offlineUUID = UUID.nameUUIDFromBytes(
                name.getBytes(StandardCharsets.UTF_8));
        return Optional.of(new GameProfile(offlineUUID, name));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockState blockState = level.getBlockState(context.getClickedPos());
        Block clickedBlock = blockState.getBlock();

        if (clickedBlock.equals(NeosPuppets.PUPPET.get())) {
            if (blockState.getValue(Puppet.SKIN) == Skin.PUPPET) {
                if (context.getItemInHand().has(NeosPuppets.SKIN_COMPONENT)) {
                    if (!level.isClientSide()) {
                        CompletableFuture.supplyAsync(() -> resolveProfile(level.getServer(), context.getItemInHand().get(NeosPuppets.SKIN_COMPONENT)))
                                        .thenAccept(profileOptional -> level.getServer().execute(() -> {
                                            profileOptional.ifPresentOrElse(profile -> {
                                                level.setBlockAndUpdate(context.getClickedPos(), NeosPuppets.CUSTOM_PUPPET_BLOCK.get().defaultBlockState()
                                                        .setValue(BlockStateProperties.HORIZONTAL_FACING, blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)));
                                                CustomPuppetBlockEntity puppetEntity = (CustomPuppetBlockEntity) level.getBlockEntity(context.getClickedPos());
                                                puppetEntity.setPose(blockState.getValue(Puppet.POSE));
                                                puppetEntity.setSkinId(profile.getId().toString());

                                                if (!context.getPlayer().isCreative()) {
                                                    context.getItemInHand().shrink(1);
                                                }
                                                context.getPlayer().playNotifySound(SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 1f, 1f);
                                            }, () -> {
                                                if (context.getPlayer() == null) {
                                                    context.getPlayer().displayClientMessage(Component.translatable("message.neospuppets.pattern_fabric_unknown_profile_warning"), true);
                                                }
                                            });
                                        }));
                    }
                    return InteractionResult.SUCCESS;
                } else {
                    context.getPlayer().displayClientMessage(Component.translatable("message.neospuppets.pattern_fabric_bing_warning"), true);
                }
            }
        }
        return InteractionResult.FAIL;
    }
}
