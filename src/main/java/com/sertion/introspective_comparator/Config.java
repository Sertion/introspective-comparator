package com.sertion.introspective_comparator;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOW_TOOLTIPS = BUILDER
        .comment("Show tooltips for the mod's items")
        .define("show_tooltips", true);

    static final ModConfigSpec SPEC = BUILDER.build();
}
