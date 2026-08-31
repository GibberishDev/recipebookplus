package com.gibbdev.recipebookplus;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.client.ConfigScreenFactoryRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public class RecipeBookPlusClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ConfigScreenFactoryRegistry.INSTANCE.register(Constants.MOD_ID, ConfigurationScreen::new);
    }
}
