package com.neoloxal.neospuppets.datagen;

import com.neoloxal.neospuppets.NeosPuppets;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Set;

public class PuppetLootTableProvider extends BlockLootSubProvider {
    protected PuppetLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(NeosPuppets.PUPPET.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return NeosPuppets.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
