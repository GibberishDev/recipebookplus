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
}
