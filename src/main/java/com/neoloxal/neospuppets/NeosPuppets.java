package com.neoloxal.neospuppets;

import com.neoloxal.neospuppets.client.PuppetModel;
import com.neoloxal.neospuppets.client.renderer.PuppetRenderer;
import com.neoloxal.neospuppets.puppets.CustomPuppetBlockEntity;
import com.neoloxal.neospuppets.puppets.CustomPuppetBlock;
import com.neoloxal.neospuppets.puppets.Puppet;
import com.neoloxal.neospuppets.puppets.PuppetManipulator;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
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
    ));
    public static final DeferredItem<Item> CUSTOM_PUPPET_ITEM = ITEMS.register("custom_puppet", () -> new BlockItem(CUSTOM_PUPPET_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockEntityType<CustomPuppetBlockEntity>> CUSTOM_PUPPET_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "custom_puppet_entity",
            () -> BlockEntityType.Builder.of(
                    CustomPuppetBlockEntity::new, CUSTOM_PUPPET_BLOCK.get()).build(null));

    public static final DeferredItem<Item> PUPPET_MANIPULATOR = ITEMS.register("puppet_manipulator", () -> new PuppetManipulator(new Item.Properties()
            .durability(250)
    ));

    // The constructor for the mod class is the first code run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public NeosPuppets(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);

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
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(PUPPET_MANIPULATOR);
        } else if (event.getTabKey() == CreativeModeTabs.OP_BLOCKS) {
            event.accept(CUSTOM_PUPPET_ITEM);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
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
    }
}
