package com.sertion.introspective_comparator.datagen;

import com.sertion.introspective_comparator.IntrospectiveComparator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = IntrospectiveComparator.MOD_ID)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherDataClient(GatherDataEvent.Client event) {
        gatherDataCommon(event);
    }
    @SubscribeEvent
    public static void gatherDataServer(GatherDataEvent.Server event) {
        gatherDataCommon(event);
    }

    private static void gatherDataCommon(GatherDataEvent event) {
        event.createProvider(IntrospectiveComparatorBlockLootTableProvider::getSubProvider);
        event.createProvider(IntrospectiveComparatorRecipeProvider.Runner::new);
        event.createProvider(IntrospectiveComparatorModelProvider::new);
    }
}
