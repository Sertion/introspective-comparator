package com.sertion.introspective_comparator;

import com.mojang.logging.LogUtils;
import com.sertion.introspective_comparator.block.custom.IntrospectiveComparatorBlock;
import com.sertion.introspective_comparator.block.custom.IntrospectiveComparatorInvertedBlock;
import com.sertion.introspective_comparator.block.custom.entity.IntrospectiveComparatorBlockEntity;
import com.sertion.introspective_comparator.block.custom.entity.IntrospectiveComparatorInvertedBlockEntity;
import com.sertion.introspective_comparator.item.custom.ItemWithToolTip;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.util.function.Supplier;


@Mod(IntrospectiveComparator.MOD_ID)
public class IntrospectiveComparator {
    public static final String MOD_ID = "introspective_comparator";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    public static final DeferredRegister<@NonNull BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, IntrospectiveComparator.MOD_ID);

    public static final DeferredBlock<@NonNull Block> INTROSPECTIVE_COMPARATOR = IntrospectiveComparatorBlock.registerTo(BLOCKS, ITEMS);
    public static final DeferredBlock<@NonNull Block> INTROSPECTIVE_COMPARATOR_INVERTED = IntrospectiveComparatorInvertedBlock.registerTo(BLOCKS, ITEMS);

    public static final ItemAbility WRENCH_CONFIGURE_ABILITY = ItemAbility.get("wrench_configure");

    public static final Supplier<@NonNull BlockEntityType<@NonNull IntrospectiveComparatorBlockEntity>> INTROSPECTIVE_COMPARATOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "introspective_comparator_block_entity",
            () -> new BlockEntityType<>(IntrospectiveComparatorBlockEntity::new, INTROSPECTIVE_COMPARATOR.get())
    );
    public static final Supplier<@NonNull BlockEntityType<@NonNull IntrospectiveComparatorInvertedBlockEntity>> INTROSPECTIVE_COMPARATOR_INVERTED_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "introspective_comparator_inverted_block_entity",
            () -> new BlockEntityType<>(IntrospectiveComparatorInvertedBlockEntity::new, INTROSPECTIVE_COMPARATOR_INVERTED.get())
    );

    public IntrospectiveComparator(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(this::addCreative);

        NeoForge.EVENT_BUS.register(IntrospectiveComparatorEvents.class);

        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(INTROSPECTIVE_COMPARATOR.value());
            event.accept(INTROSPECTIVE_COMPARATOR_INVERTED.value());
        }
    }

    private static class IntrospectiveComparatorEvents {
        @SubscribeEvent
        private static void addToolTipInformation(ItemTooltipEvent event) {
            if (Config.SHOW_TOOLTIPS.getAsBoolean() && event.getItemStack().getItem() instanceof ItemWithToolTip item) {
                item.addToolTip(event.getToolTip());
            }
        }
    }

}
