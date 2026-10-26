package com.sertion.introspective_comparator.datagen;

import com.sertion.introspective_comparator.IntrospectiveComparator;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = IntrospectiveComparator.MOD_ID)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherDataRegistryEntries(GatherDataRegistryEntriesEvent event) {
        event.gatherFor(IntrospectiveComparator.MOD_ID, Identifier.DEFAULT_NAMESPACE)
            .add(
                Registries.LOOT_TABLE,
                new LootTableProvider(
                    Set.of(),
                    List.of(
                        new LootTableProvider.SubProviderEntry(IntrospectiveComparatorBlockLootTableProvider::new, LootContextParamSets.BLOCK)
                    )
                )
            )
            .add(IntrospectiveComparatorRecipeProvider.create());
    }
    @SubscribeEvent
    public static void gatherDataClient(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        generator.addProvider(true, new IntrospectiveComparatorModelProvider(generator.getPackOutput()));
    }
}
