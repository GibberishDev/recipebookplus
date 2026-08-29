package com.gibbdev.recipebookplus;

import com.gibbdev.recipebookplus.interfaces.IConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class FabricConfig implements IConfig {
    public static boolean modEnabled = true;
    @Override public boolean getModEnabled() {
        return modEnabled;
    }
    public static String ingredientPrefix = "$";
    @Override public String getIngredientPrefix() {
        return ingredientPrefix;
    }
    public static String modidPrefix = "@";
    @Override public String getModidPrefix() {
        return modidPrefix;
    }
    public static boolean useCustomUI = true;
    @Override public boolean getUseCustomUI() {return useCustomUI;}
    public static boolean displayHelpButton = true;
    @Override public boolean getDisplayHelpButton() {return displayHelpButton;}
    public static boolean enableRecipeBrowser = true;
    @Override public boolean getEnableRecipeBrowser() {return enableRecipeBrowser;}

    private static class Data {
        boolean modEnabled = true;
        String ingredientPrefix = "$";
        String modidPrefix = "@";
        boolean useCustomUI = true;
        boolean displayHelpButton = true;
        boolean enableRecipeBrowser = true;
    }

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("recipebookplus.json");

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Data data = new Data();
            data.modEnabled = modEnabled;
            data.ingredientPrefix = ingredientPrefix;
            data.modidPrefix = modidPrefix;
            data.useCustomUI = useCustomUI;
            data.displayHelpButton = displayHelpButton;
            data.enableRecipeBrowser = enableRecipeBrowser;
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {Constants.LOG.error(e.getMessage());}
    }
    public static void load() {
        if (!Files.exists(FILE)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(FILE)) {
            Data data = GSON.fromJson(reader, Data.class);
            if (data != null) {
                modEnabled = data.modEnabled;
                ingredientPrefix = data.ingredientPrefix;
                modidPrefix = data.modidPrefix;
                useCustomUI = data.useCustomUI;
                displayHelpButton = data.displayHelpButton;
                enableRecipeBrowser = data.enableRecipeBrowser;
            }
        } catch (IOException e) {Constants.LOG.error(e.getMessage());}
    }
}
