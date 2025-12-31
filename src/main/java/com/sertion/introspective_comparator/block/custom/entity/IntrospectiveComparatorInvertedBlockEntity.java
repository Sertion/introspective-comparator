package com.sertion.introspective_comparator.block.custom.entity;


import com.sertion.introspective_comparator.IntrospectiveComparator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public class IntrospectiveComparatorInvertedBlockEntity extends BlockEntity implements PowerHavingBlockEntity {
    private int power = 15;

    public IntrospectiveComparatorInvertedBlockEntity(BlockPos pos, BlockState blockState) {
        super(IntrospectiveComparator.INTROSPECTIVE_COMPARATOR_INVERTED_BLOCK_ENTITY.get(), pos, blockState);
    }

    public final int getPower() {
        return this.power;
    }

    public final void setPower(int power) {
        this.power = power;
    }

    protected final void loadAdditional(@NotNull ValueInput in) {
        super.loadAdditional(in);
        this.power = in.getIntOr("power", 15);
    }

    protected final void saveAdditional(@NotNull ValueOutput out) {
        super.saveAdditional(out);
        out.putInt("power", this.power);
    }
}

