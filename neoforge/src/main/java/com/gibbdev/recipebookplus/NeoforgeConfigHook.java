package com.gibbdev.recipebookplus;

import com.gibbdev.recipebookplus.interfaces.IConfig;

public class NeoforgeConfigHook implements IConfig {
    @Override
    public boolean getModEnabled() {
        return NeoforgeConfig.MOD_ENABLED.get();
    }
    @Override
    public String getIngredientPrefix() {
        return NeoforgeConfig.INGREDIENT_PREFIX.get();
    }
    @Override
    public String getModidPrefix() {
        return NeoforgeConfig.MODID_PREFIX.get();
    }
    @Override
    public boolean getUseCustomUI() {
        return NeoforgeConfig.USE_CUSTOM_UI.get();
    }
    @Override
    public boolean getDisplayHelpButton() {
        return NeoforgeConfig.DISPLAY_HELP_BUTTON.get();
    }
    @Override
    public boolean getEnableRecipeBrowser() {
        return NeoforgeConfig.ENABLE_RECIPE_BROWSER.get();
    }
    @Override
    public boolean getRecipeDiscovery() {
        return NeoforgeConfig.RECIPE_DISCOVERY.get();
    }
    @Override
    public RECIPE_DISCOVERY_MODE_ENUM getRecipeDiscoveryMode() {
        return NeoforgeConfig.RECIPE_DISCOVERY_MODE.get();
    }
}
