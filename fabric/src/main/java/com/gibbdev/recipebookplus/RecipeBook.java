package com.gibbdev.recipebookplus;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;

public class RecipeBook implements ModInitializer {
    
    @Override
    public void onInitialize() {
//        FabricConfig.load();
        AutoConfig.register(FabricConfig.class, JanksonConfigSerializer::new);
        Config.INSTANCE = AutoConfig.getConfigHolder(FabricConfig.class).getConfig();
    }
}
