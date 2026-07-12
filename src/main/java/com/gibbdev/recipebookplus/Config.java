package com.gibbdev.recipebookplus;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue MOD_ENABLED = BUILDER
            .translation("recipebookplus.configuration.mod_enabled")
            .comment("Determines if mod is enabled","If \"OFF\" reverts the recipe book to vanilla behavior")
            .define("mod_enabled", true);

    static final ModConfigSpec SPEC = BUILDER.build();
}
