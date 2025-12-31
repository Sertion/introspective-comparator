package com.sertion.introspective_comparator.datagen;

import com.sertion.introspective_comparator.IntrospectiveComparator;
import com.sertion.introspective_comparator.block.custom.IntrospectiveComparatorBlock;
import com.sertion.introspective_comparator.block.custom.IntrospectiveComparatorInvertedBlock;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.jetbrains.annotations.NotNull;

public class IntrospectiveComparatorModelProvider extends ModelProvider {
    public IntrospectiveComparatorModelProvider(PackOutput output) {
        super(output, IntrospectiveComparator.MOD_ID);
    }

    @Override
    protected void registerModels(@NotNull BlockModelGenerators blockModels, @NotNull ItemModelGenerators itemModels) {
        blockModels.blockStateOutput.accept(
            faceAttachedVariant(
                comparatorVariants(
                    startMultiVariant(IntrospectiveComparator.INTROSPECTIVE_COMPARATOR),
                    blockPath(IntrospectiveComparatorBlock.NAME)
                )
            )
        );
        blockModels.blockStateOutput.accept(
            faceAttachedVariant(
                comparatorVariants(
                    startMultiVariant(IntrospectiveComparator.INTROSPECTIVE_COMPARATOR_INVERTED),
                    blockPath(IntrospectiveComparatorInvertedBlock.NAME)
                )
            )
        );

        simpleBlockItem(blockModels, IntrospectiveComparator.INTROSPECTIVE_COMPARATOR, IntrospectiveComparatorBlock.NAME);
        simpleBlockItem(blockModels, IntrospectiveComparator.INTROSPECTIVE_COMPARATOR_INVERTED, IntrospectiveComparatorInvertedBlock.NAME);
    }

    private @NotNull MultiVariantGenerator startMultiVariant(DeferredBlock<?> block) {
        Variant introspectiveComparatorVariant = new Variant(block.getId());
        return MultiVariantGenerator.dispatch(block.get(), BlockModelGenerators.variant(introspectiveComparatorVariant));
    }

    private @NotNull MultiVariantGenerator faceAttachedVariant(@NotNull MultiVariantGenerator generator) {
        return generator
            .with(
                PropertyDispatch.modify(BlockStateProperties.ATTACH_FACE)
                    .select(AttachFace.CEILING, BlockModelGenerators.X_ROT_270)
                    .select(AttachFace.WALL, BlockModelGenerators.NOP)
                    .select(AttachFace.FLOOR, BlockModelGenerators.X_ROT_90)
            ).with(
                PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
                    .select(Direction.NORTH, BlockModelGenerators.NOP)
                    .select(Direction.EAST, BlockModelGenerators.Y_ROT_90)
                    .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180)
                    .select(Direction.WEST, BlockModelGenerators.Y_ROT_270)
            );
    }

    private @NotNull MultiVariantGenerator comparatorVariants(@NotNull MultiVariantGenerator generator, @NotNull String modelPath) {
        return generator
            .with(
                PropertyDispatch.modify(BlockStateProperties.MODE_COMPARATOR, BlockStateProperties.POWERED)
                    .select(ComparatorMode.COMPARE, false, (v) -> v.withModel(Identifier.fromNamespaceAndPath(modId, modelPath)))
                    .select(ComparatorMode.COMPARE, true, (v) -> v.withModel(Identifier.fromNamespaceAndPath(modId, modelPath + "_on")))
                    .select(ComparatorMode.SUBTRACT, false, (v) -> v.withModel(Identifier.fromNamespaceAndPath(modId, modelPath + "_subtract")))
                    .select(ComparatorMode.SUBTRACT, true, (v) -> v.withModel(Identifier.fromNamespaceAndPath(modId, modelPath + "_subtract_on")))
            );
    }

    private String blockPath(String blockName) {
        return "block/" + blockName;
    }

    private void simpleBlockItem(@NotNull BlockModelGenerators blockModels, DeferredBlock<?> block, String blockName) {
        blockModels.registerSimpleItemModel(block.get(), Identifier.fromNamespaceAndPath(modId, blockPath(blockName)));
    }
}
