package com.gibbdev.recipebookplus;

import com.gibbdev.recipebookplus.interfaces.IConfig.RECIPE_DISCOVERY_MODE_ENUM;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.awt.*;

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
    public static final ModConfigSpec.BooleanValue ENABLE_RECIPE_BROWSER = BUILDER
            .translation("recipebookplus.configuration.enable_recipe_browser")
            .comment("If \"ON\" then there will be UI option to view all known recipes across different recipe types")
            .define("display_help_button", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();


    public static final ModConfigSpec.BooleanValue RECIPE_DISCOVERY = SERVER_BUILDER
            .translation("recipebookplus.configuration.recipe_discovery")
            .comment("If \"ON\" then recipes will only be shown if player has discovered them. Server authoritative setting. can ve change on client if server does not have mod installed")
            .define("recipe_discovery", true);
    public static final ModConfigSpec.EnumValue<RECIPE_DISCOVERY_MODE_ENUM> RECIPE_DISCOVERY_MODE = SERVER_BUILDER
            .translation("recipebookplus.configuration.recipe_discovery_mode")
            .comment("Defines recipe discovery mode:",
                    "§6INGREDIENT_AND_ITEM§r:\ndiscover recipe from inventory change - any ingredient or item itself [DEFAULT]",
                    "§6ITEM§r:\ndiscover recipe from inventory change - item itself",
                    "§6INGREDIENT§r:\ndiscover recipe from inventory change - any ingredient",
                    "§6ADVANCEMENT§r:\ndiscover recipe from advancement trigger - same way vanilla treats recipe discovery",
                    "§6NONE§r:\ndo not discover recipes - can only be awarded with §u/rbp give recipe <target?> <id>§r§8"
            )
            .defineEnum("recipe_discovery_mode", RECIPE_DISCOVERY_MODE_ENUM.INGREDIENT_AND_ITEM);

    static final ModConfigSpec SERVER_SPEC = SERVER_BUILDER.build();
}
