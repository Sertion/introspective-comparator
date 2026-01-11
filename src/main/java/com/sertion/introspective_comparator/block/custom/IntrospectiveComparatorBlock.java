package com.sertion.introspective_comparator.block.custom;

import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.MapCodec;
import com.sertion.introspective_comparator.IntrospectiveComparator;
import com.sertion.introspective_comparator.block.custom.entity.IntrospectiveComparatorBlockEntity;
import com.sertion.introspective_comparator.item.custom.BlockItemWithToolTip;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;
import java.util.stream.Stream;

public class IntrospectiveComparatorBlock extends Block implements EntityBlock {
    public final static String NAME = "introspective_comparator";
    public final static MapCodec<IntrospectiveComparatorBlock> CODEC = simpleCodec(IntrospectiveComparatorBlock::new);

    public final static EnumProperty<@NotNull AttachFace> ATTACH_FACE = BlockStateProperties.ATTACH_FACE;
    public final static EnumProperty<@NotNull Direction> HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    public final static BooleanProperty POWERED = BlockStateProperties.POWERED;
    public final static EnumProperty<@NotNull ComparatorMode> MODE_COMPARATOR = BlockStateProperties.MODE_COMPARATOR;

    private final static VoxelShape SOUTH_FLOOR_SHAPE = Shapes.join(
        Shapes.join(
            Block.box(0, 0, 0, 16, 9, 2),
            Block.box(0, 0, 7, 16, 9, 9),
            BooleanOp.OR
        ),
        Block.box(0, 7, 2, 16, 9, 7),
        BooleanOp.OR
    );

    protected final static VoxelShape[] shapes = Stream.of(
        OctahedralGroup.IDENTITY,                                                 // Floor South
        OctahedralGroup.BLOCK_ROT_Y_90,                                           // Floor West
        OctahedralGroup.BLOCK_ROT_Y_180,                                          // Floor North
        OctahedralGroup.BLOCK_ROT_Y_270,                                          // Floor East
        OctahedralGroup.IDENTITY       .compose(OctahedralGroup.BLOCK_ROT_X_90),  // Wall South
        OctahedralGroup.BLOCK_ROT_Y_90 .compose(OctahedralGroup.BLOCK_ROT_X_90),  // Wall West
        OctahedralGroup.BLOCK_ROT_Y_180.compose(OctahedralGroup.BLOCK_ROT_X_90),  // Wall North
        OctahedralGroup.BLOCK_ROT_Y_270.compose(OctahedralGroup.BLOCK_ROT_X_90),  // Wall East
        OctahedralGroup.BLOCK_ROT_X_180.compose(OctahedralGroup.BLOCK_ROT_Y_180), // Ceiling North
        OctahedralGroup.BLOCK_ROT_X_180.compose(OctahedralGroup.BLOCK_ROT_Y_90),  // Ceiling East
        OctahedralGroup.BLOCK_ROT_X_180.compose(OctahedralGroup.IDENTITY),        // Ceiling South
        OctahedralGroup.BLOCK_ROT_X_180.compose(OctahedralGroup.BLOCK_ROT_Y_270)  // Ceiling West
    ).map(og -> Shapes.rotate(SOUTH_FLOOR_SHAPE, og)).toArray(VoxelShape[]::new);

    public static DeferredBlock<@NotNull Block> registerTo(@NotNull DeferredRegister.Blocks registry, DeferredRegister.Items itemRegistry) {
        DeferredBlock<@NotNull Block> block = registry.registerBlock(NAME, IntrospectiveComparatorBlock::new);
        itemRegistry.registerItem(NAME, (props) -> new BlockItemWithToolTip(block.get(), props));
        return block;
    }

    protected final IntrospectivePowerUtil util = createUtil();

    public IntrospectiveComparatorBlock(Properties props) {
        super(props.noOcclusion().mapColor(MapColor.NONE));

        registerDefaultState(
            getStateDefinition().any()
                .setValue(HORIZONTAL_FACING, Direction.SOUTH)
                .setValue(ATTACH_FACE, AttachFace.FLOOR)
                .setValue(MODE_COMPARATOR, ComparatorMode.COMPARE)
                .setValue(POWERED, false)
        );
    }

    protected IntrospectivePowerUtil createUtil() {
        return new IntrospectivePowerUtil(
            ATTACH_FACE,
            HORIZONTAL_FACING,
            POWERED,
            MODE_COMPARATOR,
            false
        );
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        int index = util.getShapeIndex(state);
        if (index >= 0 && index < shapes.length) {
            return shapes[index];
        }

        return SOUTH_FLOOR_SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<@NotNull Block, @NotNull BlockState> builder) {
        builder.add(HORIZONTAL_FACING, ATTACH_FACE, MODE_COMPARATOR, POWERED);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        if (shouldTurnOn(level, pos, state)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        updateNeighborAttachedTo(level, pos, state);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        Direction observerDirection = Objects.requireNonNull(Blocks.OBSERVER.getStateForPlacement(context)).getValue(BlockStateProperties.FACING);
        BlockState state = defaultBlockState()
            .setValue(HORIZONTAL_FACING, util.getHorizontalForPlacement(context, observerDirection))
            .setValue(ATTACH_FACE, util.getVerticalForPlacement(observerDirection));

        if (canSurvive(state, context.getLevel(), context.getClickedPos())) {
            return state;
        }

        AttachFace clickedFace = util.getVerticalForPlacementWhenNoWall(context);
        if (clickedFace != null) {
            BlockState secondAttemptState = state.setValue(ATTACH_FACE, clickedFace);
            if (canSurvive(secondAttemptState, context.getLevel(), context.getClickedPos())) {
                return secondAttemptState;
            }
        }

        return null;
    }

    @Override
    protected boolean propagatesSkylightDown(@NotNull BlockState state) {
        return true;
    }

    @Override
    protected boolean isSignalSource(@NotNull BlockState state) {
        return true;
    }

    @Override
    public boolean getWeakChanges(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        return true;
    }

    @Override
    protected boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        if (util.canSurviveOnAttachedOrFloor(state, level, pos)) {
            return true;
        }
        BlockState facingBlock = level.getBlockState(pos.relative(util.getFacing(state)));
        return !facingBlock.is(Blocks.AIR) && !facingBlock.is(IntrospectiveComparator.INTROSPECTIVE_COMPARATOR);
    }

    protected boolean shouldTurnOn(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        return util.shouldTurnOn(level, pos, state);
    }

    @Override
    protected void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource randomSource) {
        refreshOutputState(state, level, pos);
    }

    protected void refreshOutputState(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        if (!util.hasPoweredChanged(state, level, pos)) {
            return;
        }

        boolean newPowered = shouldTurnOn(level, pos, state);
        boolean oldPowered = state.getValue(POWERED);
        util.setStoredPower(level, pos, state);
        if (oldPowered != newPowered) {
            level.setBlock(pos, state.setValue(POWERED, newPowered), Block.UPDATE_CLIENTS);
        }

        updateNeighborAttachedTo(level, pos, state);
    }

    protected void updateNeighborAttachedTo(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        Direction facing = util.getFacing(state);
        if (EventHooks.onNeighborNotify(level, pos, level.getBlockState(pos), EnumSet.of(facing), false).isCanceled()) {
            return;
        }
        BlockPos neighbourPos = pos.relative(facing);
        Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, facing, null);
        level.neighborChanged(neighbourPos, this, orientation);
        level.updateNeighborsAt(neighbourPos, this, orientation);
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        boolean canSurvive = state.canSurvive(level, pos);
        if (canSurvive && level.getBlockTicks().willTickThisTick(pos, this) || level.isClientSide()) {
            return;
        }
        if (canSurvive) {
            if (util.hasPoweredChanged(state, level, pos)) {
                level.scheduleTick(pos, this, 2, TickPriority.NORMAL);
            }
            return;
        }

        dropResources(state, level, pos, state.hasBlockEntity() ? level.getBlockEntity(pos) : null);
        level.removeBlock(pos, false);

        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(pos.relative(direction), this);
        }
    }

    @Override
    public void onNeighborChange(@NotNull BlockState state, @NotNull LevelReader levelReader, @NotNull BlockPos pos, @NotNull BlockPos neighbor) {
        if (levelReader.isClientSide()) {
            return;
        }

        if (levelReader instanceof Level level) {
            state.handleNeighborChanged(level, pos, levelReader.getBlockState(neighbor).getBlock(), null, false);
        }
    }

    @Override
    protected int getSignal(BlockState blockState, @NotNull BlockGetter blockAccess, @NotNull BlockPos pos, @NotNull Direction side) {
        if (!blockState.getValue(POWERED) || !util.isSupportedOutputSide(blockState, side)) {
            return 0;
        }

        return util.getStoredPower(blockAccess, pos);
    }

    @Override
    protected int getDirectSignal(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        }

        BlockState newState = state.cycle(MODE_COMPARATOR);
        level.playSound(
            player,
            pos,
            SoundEvents.COMPARATOR_CLICK,
            SoundSource.BLOCKS,
            0.3F,
            newState.getValue(MODE_COMPARATOR) == ComparatorMode.SUBTRACT ? 0.55F : 0.5F
        );
        level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
        refreshOutputState(newState, level, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected @NotNull InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (!player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        }

        if (stack.is(Tags.Items.TOOLS_WRENCH)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        swapTo(state, level, pos);
        updateNeighborAttachedTo(level, pos, state);

        return InteractionResult.SUCCESS;
    }

    protected void swapTo(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }

        level.removeBlock(pos, false);
        level.setBlock(
            pos,
            getWrenchBlockReplacement()
                .defaultBlockState()
                .setValue(ATTACH_FACE, state.getValue(ATTACH_FACE))
                .setValue(HORIZONTAL_FACING, state.getValue(HORIZONTAL_FACING))
                .setValue(MODE_COMPARATOR, state.getValue(MODE_COMPARATOR)),
            Block.UPDATE_CLIENTS
        );
    }

    protected Block getWrenchBlockReplacement() {
        return IntrospectiveComparator.INTROSPECTIVE_COMPARATOR_INVERTED.get();
    }

    @Override
    protected void affectNeighborsAfterRemoval(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, boolean movedByPiston) {
        if (movedByPiston) {
            return;
        }
        updateNeighborAttachedTo(level, pos, state);
    }

    @Override
    protected @NotNull MapCodec<? extends IntrospectiveComparatorBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new IntrospectiveComparatorBlockEntity(blockPos, blockState);
    }
}
