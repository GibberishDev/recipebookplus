package com.gibbdev.recipebookplus;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(Constants.MOD_ID)
public class NeoforgeRecipeBookPlus {
    public NeoforgeRecipeBookPlus(IEventBus ignored, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.Client.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.Server.SPEC);
    }
}