package com.sertion.introspective_comparator.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class BlockItemWithToolTip extends BlockItem implements ItemWithToolTip {
    public BlockItemWithToolTip(Block block, Properties properties) {
        super(block, properties);
    }

    public void addToolTip(List<Component> toolTip) {
        this.createToolTip(toolTip, descriptionId);
    }
}
