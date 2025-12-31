package com.sertion.introspective_comparator.block.custom;

import com.sertion.introspective_comparator.block.custom.entity.PowerHavingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record IntrospectivePowerUtil(
        EnumProperty<@NotNull AttachFace> face,
        EnumProperty<@NotNull Direction> horizontal,
        BooleanProperty powered,
        EnumProperty<@NotNull ComparatorMode> mode,
        boolean inverted
    ) {

    public Direction getFacing(@NotNull BlockState state) {
        Direction verticalFacing = faceToDirection(state.getValue(face));
        if (verticalFacing != null) {
            return verticalFacing;
        }
        return state.getValue(horizontal);
    }

    public int getShapeIndex(@NotNull BlockState state) {
        return faceToOffset(state.getValue(face)) + state.getValue(horizontal).get2DDataValue();
    }

    public boolean canSurviveOnAttachedOrFloor(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        if (AttachFace.WALL != state.getValue(face)) {
            return false;
        }

        return !level.getBlockState(pos.relative(Direction.DOWN)).is(Blocks.AIR)
            || !level.getBlockState(pos.relative(state.getValue(horizontal))).is(Blocks.AIR);
    }

    public Direction getHorizontalForPlacement(@NotNull BlockPlaceContext context, @NotNull Direction vanillaFacing) {
        if (vanillaFacing != Direction.DOWN && vanillaFacing != Direction.UP) {
            return vanillaFacing;
        }
        return context.getHorizontalDirection();
    }

    public AttachFace getVerticalForPlacement(@NotNull Direction vanillaFacing) {
        return switch (vanillaFacing) {
            case UP -> AttachFace.CEILING;
            case DOWN -> AttachFace.FLOOR;
            default -> AttachFace.WALL;
        };
    }

    public @Nullable AttachFace getVerticalForPlacementWhenNoWall(@NotNull BlockPlaceContext context) {
        return switch (context.getClickedFace().getOpposite()) {
            case UP -> AttachFace.CEILING;
            case DOWN -> AttachFace.FLOOR;
            default -> null;
        };
    }

    public void setStoredPower(@NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull BlockState state) {
        if (level.getBlockEntity(pos) instanceof PowerHavingBlockEntity powerHavingBlockEntity) {
            powerHavingBlockEntity.setPower(getOutputPower(level, pos, state));
        }
    }

    public int getStoredPower(@NotNull BlockGetter getter, @NotNull BlockPos pos) {
        return getter.getBlockEntity(pos) instanceof PowerHavingBlockEntity powerHavingBlockEntity
            ? powerHavingBlockEntity.getPower()
            : 0;
    }

    public boolean hasPoweredChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        int newPower = getOutputPower(level, pos, state);
        int storedPower = getStoredPower(level, pos);
        return newPower != storedPower;
    }

    public boolean shouldTurnOn(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        return getOutputPower(level, pos, state) != 0;
    }


    public boolean isSupportedOutputSide(@NotNull BlockState state, @NotNull Direction side) {
        return switch (state.getValue(face)) {
            case WALL -> state.getValue(horizontal).getOpposite() == side;
            case FLOOR -> side == Direction.UP;
            case CEILING -> side == Direction.DOWN;
        };
    }

    private int getInputPower(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        Direction direction = getFacing(state);
        BlockPos blockpos = pos.relative(direction);
        BlockState blockstate = level.getBlockState(blockpos);
        if (blockstate.hasAnalogOutputSignal()) {
            return blockstate.getAnalogOutputSignal(level, blockpos, direction.getOpposite());
        }

        if (blockstate.isRedstoneConductor(level, blockpos)) {
            BlockPos twoAwayBlockPos = blockpos.relative(direction);
            BlockState twoAwayState = level.getBlockState(twoAwayBlockPos);
            if (twoAwayState.hasAnalogOutputSignal()) {
                return twoAwayState.getAnalogOutputSignal(level, twoAwayBlockPos, direction.getOpposite());
            }
        }

        return 0;
    }

    private int getSidePower(@NotNull SignalGetter level, @NotNull BlockPos pos, @NotNull BlockState state) {
        Direction facing = state.getValue(horizontal);
        Direction right = facing.getClockWise();
        Direction left = facing.getCounterClockWise();
        return Math.max(
                level.getControlInputSignal(pos.relative(right), right, false),
                level.getControlInputSignal(pos.relative(left), left, false)
        );
    }

    private int getOutputPower(@NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull BlockState state) {
        int rawPower = getInputPower((Level) level, pos, state);
        int inputPower = inverted ? 15 - rawPower : rawPower;
        if (inputPower == 0) {
            return 0;
        }

        int sidePower = getSidePower((SignalGetter) level, pos, state);
        if (state.getValue(mode) == ComparatorMode.COMPARE) {
            return inputPower >= sidePower ? inputPower : 0;
        }

        return Math.max(inputPower - sidePower, 0);
    }

    private Direction faceToDirection(@NotNull AttachFace attachFace) {
        return switch (attachFace) {
            case CEILING -> Direction.UP;
            case FLOOR -> Direction.DOWN;
            default -> null;
        };
    }

    private int faceToOffset(@NotNull AttachFace attachFace) {
        return switch (attachFace) {
            case CEILING -> 2;
            case FLOOR -> 0;
            default -> 1;
        } * horizontal.getPossibleValues().size();
    }
}
