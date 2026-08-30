package com.gibbdev.recipebookplus.interfaces;

public interface IConfig {


    enum RECIPE_DISCOVERY_MODE_ENUM {
        INGREDIENT_AND_ITEM, //discover recipe from inventory change: any ingredient or item itself
        ITEM, //discover recipe from inventory change: item itself
        INGREDIENT, //discover recipe from inventory change: any ingredient
        ADVANCEMENT, //discover recipe from advancement trigger: same way vanilla treats recipe discovery
        NONE //do not discover recipes: can only be awarded with /rbp give recipe <target?> <id>
    }

    boolean getModEnabled();
    String getIngredientPrefix();
    String getModidPrefix();
    boolean getUseCustomUI();
    boolean getDisplayHelpButton();
    boolean getEnableRecipeBrowser();

    boolean getRecipeDiscovery();
    RECIPE_DISCOVERY_MODE_ENUM getRecipeDiscoveryMode();
}
