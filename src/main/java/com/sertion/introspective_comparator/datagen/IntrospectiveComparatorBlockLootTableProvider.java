package com.sertion.introspective_comparator.datagen;

import com.sertion.introspective_comparator.IntrospectiveComparator;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class IntrospectiveComparatorBlockLootTableProvider extends BlockLootSubProvider {
    protected IntrospectiveComparatorBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
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

    public static LootTableProvider getSubProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        return new LootTableProvider(
            output,
            Set.of(),
            List.of(new LootTableProvider.SubProviderEntry(
                IntrospectiveComparatorBlockLootTableProvider::new,
                LootContextParamSets.BLOCK
            )),
            provider
        );
    }
}
