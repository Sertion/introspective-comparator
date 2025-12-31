package com.sertion.introspective_comparator.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface ItemWithToolTip {
    void addToolTip(List<Component> toolTip);

    default void createToolTip(List<Component> toolTip, String descriptor) {
        String key = descriptor + ".tooltip";
        var descriptionTooltip = Component.translatable(key);
        if (!descriptionTooltip.getString().equals(key)) {
            toolTip.add(descriptionTooltip.withStyle(ChatFormatting.GRAY));
        }

        String advancedKey = key + ".advanced";
        var descriptionTooltipAdvanced = Component.translatable(advancedKey);
        if (!descriptionTooltipAdvanced.getString().equals(advancedKey)) {
            toolTip.add(descriptionTooltipAdvanced.withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
