package com.sertion.introspective_comparator.block.custom;

import com.mojang.serialization.MapCodec;
import com.sertion.introspective_comparator.IntrospectiveComparator;
import com.sertion.introspective_comparator.block.custom.entity.IntrospectiveComparatorInvertedBlockEntity;
import com.sertion.introspective_comparator.item.custom.BlockItemWithToolTip;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.NonNull;

public class IntrospectiveComparatorInvertedBlock extends IntrospectiveComparatorBlock {
    public final static String NAME = "introspective_comparator_inverted";
    public final static MapCodec<IntrospectiveComparatorInvertedBlock> CODEC = simpleCodec(IntrospectiveComparatorInvertedBlock::new);

    public static DeferredBlock<@NonNull Block> registerTo(DeferredRegister. @NonNull Blocks registry, DeferredRegister. @NonNull Items itemRegistry) {
        DeferredBlock<@NonNull Block> block = registry.registerBlock(NAME, IntrospectiveComparatorInvertedBlock::new);
        itemRegistry.registerItem(NAME, (props) -> new BlockItemWithToolTip(
            block.get(),
            props,
            () -> BuiltInRegistries.ITEM
                .stream()
                .map(ItemStack::new)
                .anyMatch((stack) -> stack.canPerformAction(IntrospectiveComparator.WRENCH_CONFIGURE_ABILITY))
        ));
        return block;
    }

    public IntrospectiveComparatorInvertedBlock(Properties props) {
        super(props);
        registerDefaultState(getStateDefinition().any().setValue(POWERED, true));
    }

    @Override
    protected IntrospectivePowerUtil createUtil() {
        return new IntrospectivePowerUtil(
            ATTACH_FACE,
            HORIZONTAL_FACING,
            POWERED,
            MODE_COMPARATOR,
            true
        );
    }

    protected Block getWrenchBlockReplacement() {
        return IntrospectiveComparator.INTROSPECTIVE_COMPARATOR.get();
    }

    @Override
    protected @NonNull MapCodec<IntrospectiveComparatorInvertedBlock> codec() {
        return CODEC;
    }

    @Override
    public @NonNull BlockEntity newBlockEntity(@NonNull BlockPos blockPos, @NonNull BlockState blockState) {
        return new IntrospectiveComparatorInvertedBlockEntity(blockPos, blockState);
    }
}
