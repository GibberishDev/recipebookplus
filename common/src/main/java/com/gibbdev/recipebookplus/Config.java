package com.gibbdev.recipebookplus;

import com.gibbdev.recipebookplus.networking.ModStatus;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.ModConfigSpec;


public class Config {

    public enum HUDOverlayAnchor {
        TOP_LEFT,
        TOP_MIDDLE,
        TOP_RIGHT,
        CENTER_LEFT,
        CENTER_MIDDLE,
        CENTER_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_MIDDLE,
        BOTTOM_RIGHT,
        NONE
    }

    public static class Client {


        public static final ModConfigSpec SPEC;

        public static final ModConfigSpec.BooleanValue MOD_ENABLED;
        public static final ModConfigSpec.ConfigValue<String> INGREDIENT_PREFIX;
        public static final ModConfigSpec.ConfigValue<String> MODID_PREFIX;
        public static final ModConfigSpec.BooleanValue USE_CUSTOM_UI;
        public static final ModConfigSpec.BooleanValue DISPLAY_HELP_BUTTON;
        public static final ModConfigSpec.EnumValue<HUDOverlayAnchor> RECIPE_OVERLAY_POSITION_ANCHOR;
        public static final ModConfigSpec.IntValue RECIPE_OVERLAY_X_OFFSET;
        public static final ModConfigSpec.IntValue RECIPE_OVERLAY_Y_OFFSET;
        public static final ModConfigSpec.BooleanValue RECIPE_DISCOVERY;
        public static final ModConfigSpec.BooleanValue RECIPE_DISCOVERY_ITEM;
        public static final ModConfigSpec.BooleanValue RECIPE_DISCOVERY_INGREDIENT;
        public static final ModConfigSpec.BooleanValue RECIPE_DISCOVERY_ADVANCEMENT;


        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
            {
                builder.push("general");
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
                RECIPE_OVERLAY_POSITION_ANCHOR = builder
                        .translation("recipebookplus.configuration.hudoverlay_anchor")
                        .comment("Determines anchor of the hud overlay element on screen. If \"NONE\" game will skip rendering the overlay\n§6Also hides vanilla new recipes toast§r")
                        .defineEnum("hudoverlay_anchor",HUDOverlayAnchor.TOP_LEFT);
                RECIPE_OVERLAY_X_OFFSET = builder
                        .translation("recipebookplus.configuration.hudoverlay_x_offset")
                        .comment("Determines horizontal offset in pixels from screen edge. can be negative")
                        .defineInRange("hudoverlay_x_offset", 10, -100000, 100000);
                RECIPE_OVERLAY_Y_OFFSET = builder
                        .translation("recipebookplus.configuration.hudoverlay_y_offset")
                        .comment("Determines vertical offset in pixels from screen edge. can be negative")
                        .defineInRange("hudoverlay_y_offset", 10, -100000, 100000);
                builder.pop();
                builder.push("server_authoritative");

                RECIPE_DISCOVERY = builder
                        .translation("recipebookplus.configuration.recipe_discovery")
                        .comment(
                                "If \"ON\" then recipes will only be shown if player has discovered them",
                                " §4OVERRIDEN IN CASE SERVER HAS MOD INSTALLED§r"
                        )
                        .define("recipe_discovery_client", true);
                RECIPE_DISCOVERY_ITEM = builder
                        .translation("recipebookplus.configuration.recipe_discovery_item")
                        .comment(
                                "If \"ON\" then player would receive recipes that have obtained item as a result",
                                " §4OVERRIDEN IN CASE SERVER HAS MOD INSTALLED§r"
                        )
                        .define("recipe_discovery_item", false);
                RECIPE_DISCOVERY_INGREDIENT = builder
                        .translation("recipebookplus.configuration.recipe_discovery_ingredient")
                        .comment(
                                "If \"ON\" then player would receive recipes that have obtained item as an ingredient",
                                " §4OVERRIDEN IN CASE SERVER HAS MOD INSTALLED§r"
                        )
                        .define("recipe_discovery_ingredient", false);
                RECIPE_DISCOVERY_ADVANCEMENT = builder
                        .translation("recipebookplus.configuration.recipe_discovery_advancement")
                        .comment(
                                "If \"ON\" then player would receive recipes that have obtained item as an ingredient",
                                " §4OVERRIDEN IN CASE SERVER HAS MOD INSTALLED§r"
                        )
                        .define("recipe_discovery_advancement", true);

                builder.pop();
            }
            SPEC = builder.build();
        }

    }

    public static class Server {
        public static final ModConfigSpec SPEC;

        public static final ModConfigSpec.BooleanValue RECIPE_DISCOVERY;
        public static final ModConfigSpec.BooleanValue RECIPE_DISCOVERY_ITEM;
        public static final ModConfigSpec.BooleanValue RECIPE_DISCOVERY_INGREDIENT;
        public static final ModConfigSpec.BooleanValue RECIPE_DISCOVERY_ADVANCEMENT;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            RECIPE_DISCOVERY = builder
                    .translation("recipebookplus.configuration.recipe_discovery_server")
                    .comment("If \"ON\" then recipes will only be shown if player has discovered them")
                    .define("recipe_discovery", true);
            RECIPE_DISCOVERY_ITEM = builder
                    .translation("recipebookplus.configuration.recipe_discovery_item_server")
                    .comment("If \"ON\" then player would receive recipes that have obtained item as a result")
                    .define("recipe_discovery_item", false);
            RECIPE_DISCOVERY_INGREDIENT = builder
                    .translation("recipebookplus.configuration.recipe_discovery_ingredient_server")
                    .comment("If \"ON\" then player would receive recipes that have obtained item as an ingredient")
                    .define("recipe_discovery_ingredient", false);
            RECIPE_DISCOVERY_ADVANCEMENT = builder
                    .translation("recipebookplus.configuration.recipe_discovery_advancement_server")
                    .comment("If \"ON\" then player would receive recipes from advancement triggers (vanilla way)")
                    .define("recipe_discovery_advancement", true);

            SPEC = builder.build();
        }

    }

    //#region Getters
    public static boolean getModEnabled() {
        return Client.MOD_ENABLED.get();
    }

    public static String  getIngredientPrefix() {
        return Client.INGREDIENT_PREFIX.get();
    }

    public static String  getModidPrefix() {
        return Client.MODID_PREFIX.get();
    }

    public static boolean getUseCustomUI() {
        return Client.USE_CUSTOM_UI.get();
    }

    public static boolean getDisplayHelpButton() {
        return Client.DISPLAY_HELP_BUTTON.get();
    }
    public static HUDOverlayAnchor getRecipeOverlayAnchor() {
        return Client.RECIPE_OVERLAY_POSITION_ANCHOR.get();
    }
    public static int getRecipeOverlayXOffset() {
        return Client.RECIPE_OVERLAY_X_OFFSET.get();
    }
    public static int getRecipeOverlayYOffset() {
        return Client.RECIPE_OVERLAY_Y_OFFSET.get();
    }

    public static boolean getRecipeDiscovery() {
        if (Minecraft.getInstance().isSingleplayer()) return Client.RECIPE_DISCOVERY.get();
        if (ModStatus.getInstalled()) {
            return Server.RECIPE_DISCOVERY.get();
        } else {
            return Client.RECIPE_DISCOVERY.get();
        }
    }

    public static boolean getRecipeDiscoveryItem() {
        if (Minecraft.getInstance().isSingleplayer()) return Client.RECIPE_DISCOVERY_ITEM.get();
        if (ModStatus.getInstalled()) {
            return Server.RECIPE_DISCOVERY_ITEM.get();
        } else {
            return Client.RECIPE_DISCOVERY_ITEM.get();
        }
    }

    public static boolean getRecipeDiscoveryIngredient() {
        if (Minecraft.getInstance().isSingleplayer()) return Client.RECIPE_DISCOVERY_INGREDIENT.get();
        if (ModStatus.getInstalled()) {
            return Server.RECIPE_DISCOVERY_INGREDIENT.get();
        } else {
            return Client.RECIPE_DISCOVERY_INGREDIENT.get();
        }
    }

    public static boolean getRecipeDiscoveryAdvancement() {
        if (Minecraft.getInstance().isSingleplayer()) return Client.RECIPE_DISCOVERY_ADVANCEMENT.get();
        if (ModStatus.getInstalled()) {
            return Server.RECIPE_DISCOVERY_ADVANCEMENT.get();
        } else {
            return Client.RECIPE_DISCOVERY_ADVANCEMENT.get();
        }
    }
    //#endregion

}
