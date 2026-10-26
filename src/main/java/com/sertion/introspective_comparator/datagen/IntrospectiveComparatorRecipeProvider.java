package com.sertion.introspective_comparator.datagen;

import com.sertion.introspective_comparator.IntrospectiveComparator;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

public class IntrospectiveComparatorRecipeProvider extends RecipeProvider {
    protected IntrospectiveComparatorRecipeProvider(BootstrapContext<Recipe<?>> output, BootstrapContext<Advancement> advancementOutput) {
        super(output, advancementOutput);
    }

    public static MultiRegistryBootstrap create() {
        return RecipeProvider.asBootstrap(IntrospectiveComparatorRecipeProvider::new);
    }

    @Override
    protected void buildRecipes() {
        shapeless(RecipeCategory.REDSTONE, IntrospectiveComparator.INTROSPECTIVE_COMPARATOR)
            .requires(Items.COMPARATOR)
            .requires(Items.BOWL)
            .unlockedBy(getHasName(Items.COMPARATOR), has(Items.COMPARATOR))
            .unlockedBy(getHasName(Items.BOWL), has(Items.BOWL))
            .save(output);
        shapeless(RecipeCategory.REDSTONE, IntrospectiveComparator.INTROSPECTIVE_COMPARATOR_INVERTED)
            .requires(IntrospectiveComparator.INTROSPECTIVE_COMPARATOR)
            .unlockedBy(getHasName(IntrospectiveComparator.INTROSPECTIVE_COMPARATOR.get()), has(IntrospectiveComparator.INTROSPECTIVE_COMPARATOR))
            .save(output, IntrospectiveComparator.MOD_ID + ":" + "invert_introspective_comparator");
        shapeless(RecipeCategory.REDSTONE, IntrospectiveComparator.INTROSPECTIVE_COMPARATOR)
            .requires(IntrospectiveComparator.INTROSPECTIVE_COMPARATOR_INVERTED)
            .unlockedBy(getHasName(IntrospectiveComparator.INTROSPECTIVE_COMPARATOR_INVERTED.get()), has(IntrospectiveComparator.INTROSPECTIVE_COMPARATOR_INVERTED))
            .save(output, IntrospectiveComparator.MOD_ID + ":" + "invert_introspective_comparator_inverted");
    }
}
