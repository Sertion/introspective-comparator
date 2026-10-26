package com.sertion.introspective_comparator.datagen;

import com.sertion.introspective_comparator.IntrospectiveComparator;
import net.minecraft.core.Holder;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

import java.util.Set;

public class IntrospectiveComparatorBlockLootTableProvider extends BlockLootSubProvider {
    protected IntrospectiveComparatorBlockLootTableProvider(LootTableSubProvider.Context output) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), output);
    }

    @Override
    protected void generate() {
        dropSelf(IntrospectiveComparator.INTROSPECTIVE_COMPARATOR.get());
        dropSelf(IntrospectiveComparator.INTROSPECTIVE_COMPARATOR_INVERTED.get());
    }

    @Override
    protected @NonNull Iterable<Block> getKnownBlocks() {
        return IntrospectiveComparator.BLOCKS.getEntries().stream().map(Holder::value).toList();
    }
}
