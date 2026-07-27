package com.gibbdev.recipebookplus;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(Constants.MOD_ID)
public class NeoforgeRecipeBookPlus {
    public NeoforgeRecipeBookPlus(IEventBus ignored, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, NeoforgeConfig.SPEC);
        Config.INSTANCE = new NeoforgeConfigHook();
    }
}