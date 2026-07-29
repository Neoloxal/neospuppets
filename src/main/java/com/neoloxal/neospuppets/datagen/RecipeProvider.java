package com.neoloxal.neospuppets.datagen;

import com.neoloxal.neospuppets.NeosPuppets;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider {
    public RecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, NeosPuppets.PUPPET_ITEM.get())
                .pattern(" P ")
                .pattern("PSP")
                .pattern(" P ")
                .define('P', ItemTags.PLANKS)
                .define('S', Items.STRING)
                .unlockedBy("has_planks", has(ItemTags.PLANKS)).save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, NeosPuppets.PUPPET_MANIPULATOR.get())
                .pattern("/S/")
                .pattern("SBS")
                .pattern("/S/")
                .define('S', Items.STRING)
                .define('/', Items.STICK)
                .define('B', Items.COBBLESTONE)
                .unlockedBy("has_string", has(Items.STRING)).save(recipeOutput);

        super.buildRecipes(recipeOutput);
    }
}
