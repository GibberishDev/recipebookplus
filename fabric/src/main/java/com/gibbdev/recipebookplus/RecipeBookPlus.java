package com.gibbdev.recipebookplus;

import net.fabricmc.api.ModInitializer;

public class RecipeBookPlus implements ModInitializer {

    @Override
    public void onInitialize() {
        FabricConfig.load();
        Config.INSTANCE = new FabricConfig();
    }
}
