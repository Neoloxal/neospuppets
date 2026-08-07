package com.neoloxal.neospuppets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.neoloxal.neospuppets.blocks.SowingTable;
import com.neoloxal.neospuppets.client.PuppetModel;
import com.neoloxal.neospuppets.client.renderer.PuppetRenderer;
import com.neoloxal.neospuppets.gui.SowingMenu;
import com.neoloxal.neospuppets.gui.SowingScreen;
import com.neoloxal.neospuppets.items.PatternFabric;
import com.neoloxal.neospuppets.packets.SowingTextPacket;
import com.neoloxal.neospuppets.blocks.puppets.CustomPuppetBlockEntity;
import com.neoloxal.neospuppets.blocks.puppets.CustomPuppetBlock;
import com.neoloxal.neospuppets.blocks.puppets.Puppet;
import com.neoloxal.neospuppets.items.PuppetManipulator;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MODID);

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
            .stacksTo(1)
    ));

    public static final DeferredItem<Item> PATTERN_FABRIC = ITEMS.register("pattern_fabric", () -> new PatternFabric(new Item.Properties()));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SKIN_COMPONENT = DATA_COMPONENTS.registerComponentType(
            "skin",
            builder -> builder
                    .persistent(Codec.STRING)
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> POSE_CLIPBOARD_COMPONENT = DATA_COMPONENTS.registerComponentType(
            "pose_clipboard",
            builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
    );

    public static final Supplier<MenuType<SowingMenu>> SOWING_MENU = MENU_TYPES.register("sowing_menu", () -> new MenuType<>(SowingMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private static Set<String> pendingFetches = new HashSet<>();
    private static Map<String, ResourceLocation> cashedProfiles = new HashMap<>();
    private static Map<String, UUID> cashedUUIDs = new HashMap<>();
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
    public static Map<String, UUID> getCashedUUIDs() {return cashedUUIDs;}

    public static void casheProfile(String skinUUID, ResourceLocation skinTexture) {
        cashedProfiles.put(skinUUID, skinTexture);
        NeosPuppets.LOGGER.debug("Cashing " + skinUUID + " as " + skinTexture);
    }

    public static void casheUUID(String skinName, UUID skinUUID) {
        cashedUUIDs.put(skinName, skinUUID);
        NeosPuppets.LOGGER.debug("Cashing " + skinName + " as " + skinUUID);
    }

    public static void decasheProfile(String skinUUID) {
        if (!FORCECASHEDPROFILES.contains(skinUUID)) {
            cashedProfiles.remove(skinUUID);
            LOGGER.debug("Decashing " + skinUUID);
        } else {
            LOGGER.warn("Cannot decashe"  + skinUUID + "!");
        }
    }

    public static void decasheUUID(String skinName) {
        cashedUUIDs.remove(skinName);
        LOGGER.debug("Decashing " + skinName);
    }

    public static Optional<UUID> resolveProfile(String name) {
        UUID cached = cashedUUIDs.get(name);
        if (cached != null) {
            return Optional.of(cached);
        }

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

            UUID uuid = UUID.fromString(dashed);
            casheUUID(name, uuid);
            return Optional.of(uuid);
        } catch (Exception e) {
            return Optional.empty();
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
        MENU_TYPES.register(modEventBus);

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
                        ResourceLocation.fromNamespaceAndPath(NeosPuppets.MODID, "puppet/generic/" + pose)));
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
                        ResourceLocation.fromNamespaceAndPath(NeosPuppets.MODID, "bound"),
                        (stack, level, entity, seed) -> stack.has(NeosPuppets.SKIN_COMPONENT) ? 1.0f : 0.0f);
            });
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(SOWING_MENU.get(), SowingScreen::new);
        }

        @SubscribeEvent
        public static void registerPayloads(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar(NeosPuppets.MODID);
            registrar.playToServer(SowingTextPacket.TYPE, SowingTextPacket.STREAM_CODEC,
                    ((payload, context) -> {
                        context.enqueueWork(() -> {
                            AbstractContainerMenu menu = context.player().containerMenu;

                            ItemStack newItem = menu.getSlot(36).getItem().copy();
                            newItem.set(SKIN_COMPONENT, payload.text());

                            menu.getSlot(37).set(newItem);
                        });
                    }));
        }
    }
}
