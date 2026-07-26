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
}
