package com.sertion.introspective_comparator.block.custom.entity;


import com.sertion.introspective_comparator.IntrospectiveComparator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

public class IntrospectiveComparatorBlockEntity extends BlockEntity implements PowerHavingBlockEntity {
    private int power = 0;

    public IntrospectiveComparatorBlockEntity(BlockPos pos, BlockState blockState) {
        super(IntrospectiveComparator.INTROSPECTIVE_COMPARATOR_BLOCK_ENTITY.get(), pos, blockState);
    }

    public IntrospectiveComparatorBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState blockState) {
        super(entityType, pos, blockState);
    }

    public final int getPower() {
        return this.power;
    }

    public final void setPower(int power) {
        this.power = power;
    }

    protected void loadAdditional(@NonNull ValueInput in) {
        super.loadAdditional(in);
        this.power = in.getIntOr("power", 0);
    }

    protected void saveAdditional(@NonNull ValueOutput out) {
        super.saveAdditional(out);
        out.putInt("power", this.power);
    }
}

