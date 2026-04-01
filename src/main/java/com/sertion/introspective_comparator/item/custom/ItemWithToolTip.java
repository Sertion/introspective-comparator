package com.sertion.introspective_comparator.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface ItemWithToolTip {
    void addToolTip(List<Component> toolTip);

    default void createToolTip(List<Component> toolTip, String descriptor, boolean addAdvanced) {
        addTextToToolTip(toolTip, descriptor + ".tooltip", ChatFormatting.GRAY);
        if (addAdvanced) {
            addTextToToolTip(toolTip, descriptor + ".tooltip.advanced", ChatFormatting.DARK_GRAY);
        }
    }

    default void addTextToToolTip(List<Component> toolTip, String descriptor, ChatFormatting formatting) {
        var text = Component.translatable(descriptor);
        if (!text.getString().equals(descriptor)) {
            toolTip.add(text.withStyle(formatting));
        }
    }
}
