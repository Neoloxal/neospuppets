package com.neoloxal.neospuppets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neoloxal.neospuppets.blocks.SowingTable;
import com.neoloxal.neospuppets.client.PuppetModel;
import com.neoloxal.neospuppets.client.renderer.PuppetRenderer;
import com.neoloxal.neospuppets.puppets.CustomPuppetBlockEntity;
import com.neoloxal.neospuppets.puppets.CustomPuppetBlock;
import com.neoloxal.neospuppets.puppets.Puppet;
import com.neoloxal.neospuppets.puppets.PuppetManipulator;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(NeosPuppets.MODID)
public class NeosPuppets {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "neospuppets";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "neospuppets" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NeosPuppets.MODID);

    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, NeosPuppets.MODID);

    public static final DeferredBlock<Block> PUPPET = BLOCKS.register("puppet", () -> new Puppet(BlockBehaviour.Properties.of()
            .sound(SoundType.WOOD)
            .mapColor(MapColor.WOOD)
            .strength(2.0F, 3.0F)
            .noOcclusion()
            .noCollission()
    ));
    public static final DeferredItem<Item> PUPPET_ITEM = ITEMS.register("puppet", () -> new BlockItem(PUPPET.get(), new Item.Properties()));

    public static final DeferredBlock<Block> CUSTOM_PUPPET_BLOCK = BLOCKS.register("custom_puppet", () -> new CustomPuppetBlock(BlockBehaviour.Properties.of()
            .sound(SoundType.WOOD)
            .mapColor(MapColor.WOOD)
            .strength(2.0F, 3.0F)
            .noOcclusion()
            .noCollission()
            .noTerrainParticles()
    ));
    public static final DeferredItem<Item> CUSTOM_PUPPET_ITEM = ITEMS.register("custom_puppet", () -> new BlockItem(CUSTOM_PUPPET_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockEntityType<CustomPuppetBlockEntity>> CUSTOM_PUPPET_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "custom_puppet_entity",
            () -> BlockEntityType.Builder.of(
                    CustomPuppetBlockEntity::new, CUSTOM_PUPPET_BLOCK.get()).build(null));

    public static final DeferredBlock<Block> SOWING_TABLE = BLOCKS.register("sowing_table", () -> new SowingTable(BlockBehaviour.Properties.of()
            .sound(SoundType.WOOD)
            .mapColor(MapColor.WOOD)
            .strength(4f, 3.0f)
    ));
    public static final DeferredItem<Item> SOWING_TABLE_ITEM = ITEMS.register("sowing_table", () -> new BlockItem(SOWING_TABLE.get(), new Item.Properties()));

    public static final DeferredItem<Item> PUPPET_MANIPULATOR = ITEMS.register("puppet_manipulator", () -> new PuppetManipulator(new Item.Properties()
            .durability(250)
    ));

    public static final DeferredItem<Item> PATTERN_FABRIC = ITEMS.register("pattern_fabric", () -> new PatternFabric(new Item.Properties()));

    public record skinRecord(String skinID, String skinName) {};

    public static final Codec<skinRecord> SKIN_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("skinId").forGetter(skinRecord::skinID),
                    Codec.STRING.fieldOf("skinName").forGetter(skinRecord::skinName)
            ).apply(instance, skinRecord::new)
    );

    public static final StreamCodec<ByteBuf, skinRecord> SKIN_RECORD_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, skinRecord::skinID,
            ByteBufCodecs.STRING_UTF8, skinRecord::skinName,
            skinRecord::new
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SKIN_COMPONENT = DATA_COMPONENTS.registerComponentType(
            "skin",
            builder -> builder
                    .persistent(Codec.STRING)
    );

    private static Set<String> pendingFetches = new HashSet<>();
    private static Map<String, ResourceLocation> cashedProfiles = new HashMap<>();
    private static final List<String> FORCECASHEDPROFILES = List.of(
            "default"
    );

    public static boolean isFetchPending(String skinId) {
        return pendingFetches.contains(skinId);
    }

    public static void markFetchPending(String skinId) {
        pendingFetches.add(skinId);
    }

    public static void clearFetchPending(String skinId) {
        pendingFetches.remove(skinId);
    }

    public static Map<String, ResourceLocation> getCashedProfiles() {return cashedProfiles;}

    public static void casheProfile(String skinUUID, ResourceLocation skinTexture) {
        cashedProfiles.put(skinUUID, skinTexture);
        NeosPuppets.LOGGER.debug("Cashing " + skinUUID + " as " + skinTexture);
    }

    public static void decasheProfile(String skinUUID) {
        if (!FORCECASHEDPROFILES.contains(skinUUID)) {
            cashedProfiles.remove(skinUUID);
            LOGGER.debug("Decashing " + skinUUID);
        } else {
            LOGGER.warn("Cannot decashe"  + skinUUID + "!");
        }
    }

    // The constructor for the mod class is the first code run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public NeosPuppets(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (NeosPuppets) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(PUPPET_ITEM);
            event.insertAfter(Items.LOOM.getDefaultInstance(), SOWING_TABLE_ITEM.asItem().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.insertAfter(Items.SHEARS.getDefaultInstance(), PUPPET_MANIPULATOR.asItem().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(Items.WRITABLE_BOOK.getDefaultInstance(), PATTERN_FABRIC.asItem().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.insertAfter(Items.GUSTER_BANNER_PATTERN.getDefaultInstance(), PATTERN_FABRIC.asItem().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        } else if (event.getTabKey() == CreativeModeTabs.OP_BLOCKS) {
            event.accept(CUSTOM_PUPPET_ITEM);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerBlockEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(CUSTOM_PUPPET_BLOCK_ENTITY.get(), PuppetRenderer::new);
        }

        @SubscribeEvent
        public static void registerModels(ModelEvent.RegisterAdditional event) {
            for (String pose : Puppet.POSES) {
                event.register(ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath("neospuppets", "puppet/generic/" + pose)));
            }
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(PuppetModel.LAYER_LOCATION, PuppetModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void registerItemProperties(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ItemProperties.register(PATTERN_FABRIC.get(),
                        ResourceLocation.fromNamespaceAndPath("neospuppets", "bound"),
                        (stack, level, entity, seed) -> stack.has(NeosPuppets.SKIN_COMPONENT) ? 1.0f : 0.0f);
            });
        }
    }
}
