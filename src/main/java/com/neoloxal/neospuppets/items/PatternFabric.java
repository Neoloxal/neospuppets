package com.neoloxal.neospuppets.items;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.neoloxal.neospuppets.NeosPuppets;
import com.neoloxal.neospuppets.blocks.puppets.CustomPuppetBlockEntity;
import com.neoloxal.neospuppets.blocks.puppets.Puppet;
import com.neoloxal.neospuppets.Skin;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    private static Optional<UUID> resolveProfile(String name) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + name))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                // 404 = name doesn't exist, or some other error — either way, no UUID to give back
                return Optional.empty();
            }

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            String rawId = json.get("id").getAsString(); // no dashes, e.g. "069a79f444e94726a5befca90e38aaf"

            // Insert dashes into the standard 8-4-4-4-12 UUID format
            String dashed = rawId.replaceFirst(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                    "$1-$2-$3-$4-$5"
            );

            return Optional.of(UUID.fromString(dashed));
        } catch (Exception e) {
            return Optional.empty();
        }
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
                        CompletableFuture.supplyAsync(() -> resolveProfile(context.getItemInHand().get(NeosPuppets.SKIN_COMPONENT)))
                                        .thenAccept(UUID -> level.getServer().execute(() -> {
                                            UUID.ifPresentOrElse(skinUUID -> {
                                                level.setBlockAndUpdate(context.getClickedPos(), NeosPuppets.CUSTOM_PUPPET_BLOCK.get().defaultBlockState()
                                                        .setValue(BlockStateProperties.HORIZONTAL_FACING, blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)));
                                                CustomPuppetBlockEntity puppetEntity = (CustomPuppetBlockEntity) level.getBlockEntity(context.getClickedPos());
                                                puppetEntity.setPose(blockState.getValue(Puppet.POSE));
                                                puppetEntity.setSkinId(skinUUID.toString());

                                                if (!context.getPlayer().isCreative()) {
                                                    context.getItemInHand().shrink(1);
                                                }
                                                context.getPlayer().playNotifySound(SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 1f, 1f);
                                            }, () -> {
                                                if (context.getPlayer() != null) {
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
