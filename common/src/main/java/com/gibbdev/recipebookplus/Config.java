package com.gibbdev.recipebookplus;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {

    public enum RECIPE_DISCOVERY_MODE_ENUM {
        INGREDIENT_AND_ITEM, //discover recipe from inventory change: any ingredient or item itself
        ITEM, //discover recipe from inventory change: item itself
        INGREDIENT, //discover recipe from inventory change: any ingredient
        ADVANCEMENT, //discover recipe from advancement trigger: same way vanilla treats recipe discovery
        NONE //do not discover recipes: can only be awarded with /rbp give recipe <target?> <id>
    }

    public static class Client {

        public static final ModConfigSpec SPEC;

        public static final ModConfigSpec.BooleanValue MOD_ENABLED;
        public static final ModConfigSpec.ConfigValue<String> INGREDIENT_PREFIX;
        public static final ModConfigSpec.ConfigValue<String> MODID_PREFIX;
        public static final ModConfigSpec.BooleanValue USE_CUSTOM_UI;
        public static final ModConfigSpec.BooleanValue DISPLAY_HELP_BUTTON;
        public static final ModConfigSpec.BooleanValue ENABLE_RECIPE_BROWSER;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
            {
                MOD_ENABLED = builder
                        .translation("recipebookplus.configuration.mod_enabled")
                        .comment("Determines if mod is enabled","If \"OFF\" reverts the recipe book to vanilla behavior")
                        .define("mod_enabled", true);
                INGREDIENT_PREFIX = builder
                        .translation("recipebookplus.configuration.ingredient_prefix")
                        .comment("Determines prefix for search string to let recipe book know you are looking for recipes that use that item","Default: \"$\"")
                        .define("ingredient_prefix","$");
                MODID_PREFIX = builder
                        .translation("recipebookplus.configuration.modid_prefix")
                        .comment("Determines prefix for search string to let recipe book know you are looking for recipes that are added by certain mod","Default: \"@\"")
                        .define("modid_prefix","@");
                USE_CUSTOM_UI = builder
                        .translation("recipebookplus.configuration.use_custom_ui")
                        .comment("Determines which recipe book UI to use. If \"ON\" then new custom ui is used. Otherwise modified vanilla one will be rendered")
                        .define("use_custom_ui", true);
                DISPLAY_HELP_BUTTON = builder
                        .translation("recipebookplus.configuration.display_help")
                        .comment("If \"ON\" then there will be help element on recipe book screen that will display controls and prefixes in a tooltip")
                        .define("display_help_button", true);
                ENABLE_RECIPE_BROWSER = builder
                        .translation("recipebookplus.configuration.enable_recipe_browser")
                        .comment("If \"ON\" then there will be UI option to view all known recipes across different recipe types")
                        .define("display_help_button", true);
            }
            SPEC = builder.build();
        }

    }

    public static class Server {
        public static final ModConfigSpec SPEC;

        public static final ModConfigSpec.BooleanValue RECIPE_DISCOVERY;
        public static final ModConfigSpec.EnumValue<RECIPE_DISCOVERY_MODE_ENUM> RECIPE_DISCOVERY_MODE;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            RECIPE_DISCOVERY = builder
                    .translation("recipebookplus.configuration.recipe_discovery")
                    .comment("If \"ON\" then recipes will only be shown if player has discovered them. Server authoritative setting. can ve change on client if server does not have mod installed")
                    .define("recipe_discovery", true);
            RECIPE_DISCOVERY_MODE = builder
                    .translation("recipebookplus.configuration.recipe_discovery_mode")
                    .comment("Defines recipe discovery mode:",
                            "§6INGREDIENT_AND_ITEM§r:\ndiscover recipe from inventory change - any ingredient or item itself [DEFAULT]",
                            "§6ITEM§r:\ndiscover recipe from inventory change - item itself",
                            "§6INGREDIENT§r:\ndiscover recipe from inventory change - any ingredient",
                            "§6ADVANCEMENT§r:\ndiscover recipe from advancement trigger - same way vanilla treats recipe discovery",
                            "§6NONE§r:\ndo not discover recipes - can only be awarded with §u/rbp give recipe <target?> <id>§r§8"
                    )
                    .defineEnum("recipe_discovery_mode", RECIPE_DISCOVERY_MODE_ENUM.INGREDIENT_AND_ITEM);
            SPEC = builder.build();
        }

    }

    public static boolean getModEnabled() {return Client.MOD_ENABLED.get();}
    public static String getIngredientPrefix() {return Client.INGREDIENT_PREFIX.get();}
    public static String getModidPrefix() {return Client.MODID_PREFIX.get();}
    public static boolean getUseCustomUI() {return Client.USE_CUSTOM_UI.get();}
    public static boolean getDisplayHelpButton() {return Client.DISPLAY_HELP_BUTTON.get();}
    public static boolean getEnableRecipeBrowser() {return Client.ENABLE_RECIPE_BROWSER.get();}

    public static boolean getRecipeDiscovery() {return Server.RECIPE_DISCOVERY.get();}
    public static RECIPE_DISCOVERY_MODE_ENUM getRecipeDiscoveryMode() {return Server.RECIPE_DISCOVERY_MODE.get();}
}
