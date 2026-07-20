package com.gibbdev.recipebookplus;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(Constants.MOD_ID)
public class RecipeBookPlus {

    public RecipeBookPlus(IEventBus ignoredEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, NeoforgeConfig.SPEC);
        Config.INSTANCE = new NeoforgeConfigHook();
    }
}