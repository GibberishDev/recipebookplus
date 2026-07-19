package com.gibbdev.recipebookplus;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModMenuConfigScreen {
    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("recipebookplus.configuration.title"));
        builder.setSavingRunnable(FabricConfig::save);
        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("recipebookplus.configuration.title"));
        ConfigEntryBuilder entries = builder.entryBuilder();

        general.addEntry(entries.startBooleanToggle(
                        Component.translatable("recipebookplus.configuration.mod_enabled"),
                        FabricConfig.modEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(value -> FabricConfig.modEnabled = value)
                .setTooltip(Component.translatable("recipebookplus.configuration.mod_enabled.tooltip"))
                .build());
        general.addEntry(entries.startStrField(
                        Component.translatable("recipebookplus.configuration.ingredient_prefix"),
                        FabricConfig.ingredientPrefix)
                .setDefaultValue("$")
                .setSaveConsumer(value -> FabricConfig.ingredientPrefix = value)
                .setTooltip(Component.translatable("recipebookplus.configuration.ingredient_prefix.tooltip"))
                .build());
        general.addEntry(entries.startStrField(
                        Component.translatable("recipebookplus.configuration.modid_prefix"),
                        FabricConfig.modidPrefix)
                .setDefaultValue("@")
                .setSaveConsumer(value -> FabricConfig.modidPrefix = value)
                .setTooltip(Component.translatable("recipebookplus.configuration.modid_prefix.tooltip"))
                .build());



        return builder.build();
    }
}
