package com.sertion.introspective_comparator.block.custom;

import com.sertion.introspective_comparator.block.custom.entity.PowerHavingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public record IntrospectivePowerUtil(
        EnumProperty<@NonNull AttachFace> face,
        EnumProperty<@NonNull Direction> horizontal,
        BooleanProperty powered,
        EnumProperty<@NonNull ComparatorMode> mode,
        boolean inverted
    ) {

    public Direction getFacing(@NonNull BlockState state) {
        Direction verticalFacing = faceToDirection(state.getValue(face));
        if (verticalFacing != null) {
            return verticalFacing;
        }
        return state.getValue(horizontal);
    }

    public int getShapeIndex(@NonNull BlockState state) {
        return faceToOffset(state.getValue(face)) + state.getValue(horizontal).get2DDataValue();
    }

    public boolean canSurviveOnAttachedOrFloor(@NonNull BlockState state, @NonNull LevelReader level, @NonNull BlockPos pos) {
        if (AttachFace.WALL != state.getValue(face)) {
            return false;
        }

        return !level.getBlockState(pos.relative(Direction.DOWN)).is(Blocks.AIR)
            || !level.getBlockState(pos.relative(state.getValue(horizontal))).is(Blocks.AIR);
    }

    public Direction getHorizontalForPlacement(@NonNull BlockPlaceContext context, @NonNull Direction vanillaFacing) {
        if (vanillaFacing != Direction.DOWN && vanillaFacing != Direction.UP) {
            return vanillaFacing;
        }
        return context.getHorizontalDirection();
    }

    public AttachFace getVerticalForPlacement(@NonNull Direction vanillaFacing) {
        return switch (vanillaFacing) {
            case UP -> AttachFace.CEILING;
            case DOWN -> AttachFace.FLOOR;
            default -> AttachFace.WALL;
        };
    }

    public @Nullable AttachFace getVerticalForPlacementWhenNoWall(@NonNull BlockPlaceContext context) {
        return switch (context.getClickedFace().getOpposite()) {
            case UP -> AttachFace.CEILING;
            case DOWN -> AttachFace.FLOOR;
            default -> null;
        };
    }

    public void setStoredPower(@NonNull BlockGetter level, @NonNull BlockPos pos, @NonNull BlockState state) {
        if (level.getBlockEntity(pos) instanceof PowerHavingBlockEntity powerHavingBlockEntity) {
            powerHavingBlockEntity.setPower(getOutputPower(level, pos, state));
        }
    }

    public int getStoredPower(@NonNull BlockGetter getter, @NonNull BlockPos pos) {
        return getter.getBlockEntity(pos) instanceof PowerHavingBlockEntity powerHavingBlockEntity
            ? powerHavingBlockEntity.getPower()
            : 0;
    }

    public boolean hasPoweredChanged(@NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos) {
        int newPower = getOutputPower(level, pos, state);
        int storedPower = getStoredPower(level, pos);
        return newPower != storedPower;
    }

    public void updatePoweredState(@NonNull Level level, @NonNull BlockState state, @NonNull BlockPos pos) {
        boolean newPowered = shouldTurnOn(level, pos, state);
        boolean oldPowered = state.getValue(powered);
        setStoredPower(level, pos, state);
        if (oldPowered != newPowered) {
            level.setBlock(pos, state.setValue(powered, newPowered), Block.UPDATE_CLIENTS);
        }
    }

    public boolean shouldTurnOn(@NonNull Level level, @NonNull BlockPos pos, @NonNull BlockState state) {
        return getOutputPower(level, pos, state) != 0;
    }

    public boolean isSupportedOutputSide(@NonNull BlockState state, @NonNull Direction side) {
        return switch (state.getValue(face)) {
            case WALL -> state.getValue(horizontal).getOpposite() == side;
            case FLOOR -> side == Direction.UP;
            case CEILING -> side == Direction.DOWN;
        };
    }

    private int getInputPower(@NonNull Level level, @NonNull BlockPos pos, @NonNull BlockState state) {
        Direction direction = getFacing(state);
        BlockPos blockpos = pos.relative(direction);
        BlockState blockstate = level.getBlockState(blockpos);
        if (blockstate.hasAnalogOutputSignal()) {
            return handleSignalInversion(
                blockstate.getAnalogOutputSignal(level, blockpos, direction.getOpposite())
            );
        }

        if (blockstate.isRedstoneConductor(level, blockpos)) {
            BlockPos twoAwayBlockPos = blockpos.relative(direction);
            BlockState twoAwayState = level.getBlockState(twoAwayBlockPos);
            if (twoAwayState.hasAnalogOutputSignal()) {
                return handleSignalInversion(
                    twoAwayState.getAnalogOutputSignal(level, twoAwayBlockPos, direction.getOpposite())
                );
            }
        }

        return handleSignalInversion(0);
    }

    private int handleSignalInversion(int power) {
        return inverted ? 15 - power : power;
    }

    private int getSidePower(@NonNull SignalGetter level, @NonNull BlockPos pos, @NonNull BlockState state) {
        Direction facing = state.getValue(horizontal);
        Direction right = facing.getClockWise();
        Direction left = facing.getCounterClockWise();
        return Math.max(
                level.getControlInputSignal(pos.relative(right), right, false),
                level.getControlInputSignal(pos.relative(left), left, false)
        );
    }

    private int getOutputPower(@NonNull BlockGetter level, @NonNull BlockPos pos, @NonNull BlockState state) {
        int inputPower = getInputPower((Level) level, pos, state);
        if (inputPower == 0) {
            return 0;
        }

        int sidePower = getSidePower((SignalGetter) level, pos, state);
        return switch (state.getValue(mode)) {
            case ComparatorMode.COMPARE -> inputPower >= sidePower ? inputPower : 0;
            case ComparatorMode.SUBTRACT -> Math.max(inputPower - sidePower, 0);
        };
    }

    private Direction faceToDirection(@NonNull AttachFace attachFace) {
        return switch (attachFace) {
            case CEILING -> Direction.UP;
            case FLOOR -> Direction.DOWN;
            default -> null;
        };
    }

    private int faceToOffset(@NonNull AttachFace attachFace) {
        return switch (attachFace) {
            case CEILING -> 2;
            case FLOOR -> 0;
            default -> 1;
        } * horizontal.getPossibleValues().size();
    }
}
