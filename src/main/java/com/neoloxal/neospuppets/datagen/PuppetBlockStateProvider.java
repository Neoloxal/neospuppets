package com.neoloxal.neospuppets.datagen;

import com.neoloxal.neospuppets.NeosPuppets;
import com.neoloxal.neospuppets.blocks.puppets.Puppet;
import com.neoloxal.neospuppets.Skin;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.List;

public class PuppetBlockStateProvider extends BlockStateProvider {
    public PuppetBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, NeosPuppets.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        registerSkin(Skin.PUPPET, false, "minecraft:block/oak_planks", Puppet.POSES);
        registerSkin(Skin.STEVE, true, "neospuppets:block/particle/steve_particles", Puppet.POSES);
        registerSkin(Skin.ALEX, true, "neospuppets:block/particle/alex_particles", Puppet.POSES);
        registerSkin(Skin.ARI, true, "neospuppets:block/particle/ari_particles", Puppet.POSES);
        registerSkin(Skin.EFE, true, "neospuppets:block/particle/efe_particles", Puppet.POSES);
        registerSkin(Skin.KAI, true, "neospuppets:block/particle/kai_particles", Puppet.POSES);
        registerSkin(Skin.MAKENA, true, "neospuppets:block/particle/makena_particles", Puppet.POSES);
        registerSkin(Skin.NOOR, true, "neospuppets:block/particle/noor_particles", Puppet.POSES);
        registerSkin(Skin.SUNNY, true, "neospuppets:block/particle/sunny_particles", Puppet.POSES);
        registerSkin(Skin.ZURI, true, "neospuppets:block/particle/zuri_particles", Puppet.POSES);

        registerSkin(Skin.NEOLOXAL, true, "neospuppets:block/particle/paint_particles", Puppet.POSES);
    }

    private void registerSkin(Skin skin, boolean useOuterLayer, @Nullable String particleOverride, List<String> poses) {
        VariantBlockStateBuilder variants = getVariantBuilder(NeosPuppets.PUPPET.get());

        for (String pose : poses) {
            BlockModelBuilder model = models().withExistingParent("puppet/" + skin.getSerializedName() + "/" + pose,
                    modLoc("puppet/generic/" + pose))
                    .texture("skin", "neospuppets:block/skin/" + skin.getSerializedName())
                    .texture("particle", "neospuppets:block/skin/" + skin.getSerializedName());
            if (useOuterLayer) {
                model.texture("outerLayer", "neospuppets:block/skin/" + skin.getSerializedName());
            }
            if (particleOverride != null && !particleOverride.isEmpty()) {
                model.texture("particle", particleOverride);
            }
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                variants.partialState()
                        .with(Puppet.POSE, poses.indexOf(pose))
                        .with(Puppet.SKIN, skin)
                        .with(BlockStateProperties.HORIZONTAL_FACING, direction)
                        .setModels(new ConfiguredModel(model, 0, (int) direction.toYRot() + 180 % 360, false));
            }
        }

    }
}
