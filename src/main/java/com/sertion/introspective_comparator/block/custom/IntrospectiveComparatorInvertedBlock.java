package com.sertion.introspective_comparator.block.custom;

import com.mojang.serialization.MapCodec;
import com.sertion.introspective_comparator.IntrospectiveComparator;
import com.sertion.introspective_comparator.block.custom.entity.IntrospectiveComparatorInvertedBlockEntity;
import com.sertion.introspective_comparator.item.custom.BlockItemWithToolTip;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public class IntrospectiveComparatorInvertedBlock extends IntrospectiveComparatorBlock {
    public final static String NAME = "introspective_comparator_inverted";
    public final static MapCodec<IntrospectiveComparatorInvertedBlock> CODEC = simpleCodec(IntrospectiveComparatorInvertedBlock::new);

    public static DeferredBlock<@NotNull Block> registerTo(@NotNull DeferredRegister.Blocks registry, DeferredRegister.Items itemRegistry) {
        DeferredBlock<@NotNull Block> block = registry.registerBlock(NAME, IntrospectiveComparatorInvertedBlock::new);
        itemRegistry.registerItem(NAME, (props) -> new BlockItemWithToolTip(block.get(), props));
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
    protected @NotNull MapCodec<IntrospectiveComparatorInvertedBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new IntrospectiveComparatorInvertedBlockEntity(blockPos, blockState);
    }
}
