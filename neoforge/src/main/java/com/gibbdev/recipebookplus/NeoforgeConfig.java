package com.gibbdev.recipebookplus;

import net.neoforged.neoforge.common.ModConfigSpec;

public class NeoforgeConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue MOD_ENABLED = BUILDER
            .translation("recipebookplus.configuration.mod_enabled")
            .comment("Determines if mod is enabled","If \"OFF\" reverts the recipe book to vanilla behavior")
            .define("mod_enabled", true);
    public static final ModConfigSpec.ConfigValue<String> INGREDIENT_PREFIX = BUILDER
            .translation("recipebookplus.configuration.ingredient_prefix")
            .comment("Determines prefix for search string to let recipe book know you are looking for recipes that use that item","Default: \"$\"")
            .define("ingredient_prefix","$");
    public static final ModConfigSpec.ConfigValue<String> MODID_PREFIX = BUILDER
            .translation("recipebookplus.configuration.modid_prefix")
            .comment("Determines prefix for search string to let recipe book know you are looking for recipes that are added by certain mod","Default: \"@\"")
            .define("modid_prefix","@");
    public static final ModConfigSpec.BooleanValue USE_CUSTOM_UI = BUILDER
            .translation("recipebookplus.configuration.use_custom_ui")
            .comment("Determines which recipe book UI to use. If \"ON\" then new custom ui is used. Otherwise modified vanilla one will be rendered")
            .define("use_custom_ui", true);
    public static final ModConfigSpec.BooleanValue DISPLAY_HELP_BUTTON = BUILDER
            .translation("recipebookplus.configuration.display_help")
            .comment("If \"ON\" then there will be help element on recipe book screen that will display controls and prefixes in a tooltip")
            .define("display_help_button", true);

    static final ModConfigSpec SPEC = BUILDER.build();
}
