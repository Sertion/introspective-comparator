package com.sertion.introspective_comparator.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.BooleanSupplier;

public class BlockItemWithToolTip extends BlockItem implements ItemWithToolTip {
    private final BooleanSupplier condition;
    private boolean hasResolvedCondition = false;
    private boolean resolvedCondition = false;

    /**
     * @param block      The block for which this item represents.
     * @param properties Item Properties.
     * @param condition  Should the advanced tool tip be shown or not.
     *                   Cached the first time the user hovers over an item stack.
     */
    public BlockItemWithToolTip(Block block, Properties properties, BooleanSupplier condition) {
        super(block, properties);
        this.condition = condition;
    }

    public void addToolTip(List<Component> toolTip) {
        if (!hasResolvedCondition) {
            hasResolvedCondition = true;
            resolvedCondition = condition.getAsBoolean();
        }
        this.createToolTip(toolTip, descriptionId, resolvedCondition);
    }
}
