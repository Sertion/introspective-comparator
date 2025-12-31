package com.sertion.introspective_comparator.datagen;

import com.sertion.introspective_comparator.IntrospectiveComparator;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class IntrospectiveComparatorRecipeProvider extends RecipeProvider {
    protected IntrospectiveComparatorRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        ShapelessRecipeBuilder
            .shapeless(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.REDSTONE, IntrospectiveComparator.INTROSPECTIVE_COMPARATOR)
            .requires(Items.COMPARATOR)
            .requires(Items.BOWL)
            .unlockedBy("has_comparator", has(Items.COMPARATOR))
            .unlockedBy("has_bowl", has(Items.BOWL))
            .save(output);
        ShapelessRecipeBuilder
            .shapeless(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.REDSTONE, IntrospectiveComparator.INTROSPECTIVE_COMPARATOR_INVERTED)
            .requires(IntrospectiveComparator.INTROSPECTIVE_COMPARATOR)
            .unlockedBy("has_introspective_comparator", has(IntrospectiveComparator.INTROSPECTIVE_COMPARATOR))
            .save(output, "introspective_comparator_to_introspective_comparator_inverted");
        ShapelessRecipeBuilder
            .shapeless(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.REDSTONE, IntrospectiveComparator.INTROSPECTIVE_COMPARATOR)
            .requires(IntrospectiveComparator.INTROSPECTIVE_COMPARATOR_INVERTED)
            .unlockedBy("has_introspective_comparator_inverted", has(IntrospectiveComparator.INTROSPECTIVE_COMPARATOR_INVERTED))
            .save(output, "introspective_comparator_inverted_to_introspective_comparator");
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected @NotNull RecipeProvider createRecipeProvider(HolderLookup.@NotNull Provider provider, @NotNull RecipeOutput output) {
            return new IntrospectiveComparatorRecipeProvider(provider, output);
        }

        @Override
        public @NotNull String getName() {
            return "IntrospectiveComparatorRecipeProvider";
        }
    }
}
