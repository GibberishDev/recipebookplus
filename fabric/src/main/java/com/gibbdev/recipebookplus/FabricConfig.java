package com.gibbdev.recipebookplus;

import com.gibbdev.recipebookplus.interfaces.IConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.network.chat.Component;


@Config(name = Constants.MOD_ID)
public class FabricConfig implements IConfig, ConfigData {


    //region client
    @ConfigEntry.Category("Client")
    @ConfigEntry.Gui.Tooltip()
    private static boolean MOD_ENABLED = true;
    @ConfigEntry.Category("Client")
    @ConfigEntry.Gui.Tooltip()
    private static String INGREDIENT_PREFIX = "$";
    @ConfigEntry.Category("Client")
    @ConfigEntry.Gui.Tooltip()
    private static String MODID_PREFIX = "@";

    //⠀⠀⠀⢀⣤⣴⣶⣶⣤⠀⠀⠀⢠⣤⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⣤⠀⠀⠀⠀⣤⣤⣤⣤⣤⣤⣤⠀⠀⠀⢠⣤⠀⠀⠀⠀⠀⠀⣤⡄⠀⠀⣠⣤⣤⣤⣤⣤⣤⣤⣤⠀
    //⠀⢀⣾⣿⠟⠋⠉⠉⠙⠁⠀⠀⣿⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⢸⣿⡟⠛⠛⠛⠛⠛⠁⠀⠀⣿⣿⣿⠀⠀⠀⠀⠀⣿⣿⠀⠀⠙⠛⠛⠛⣿⣿⠛⠛⠛⠁
    //⠀⣿⣿⠁⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⢸⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⢿⣿⠀⠀⠀⠀⣿⣿⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⠀
    //⢰⣿⡏⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⢸⣿⣇⣀⣀⣀⣀⣀⠀⠀⠀⣿⣿⠀⢿⣿⠀⠀⠀⣿⣿⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⠀
    //⢸⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⢸⣿⡿⠿⠿⠿⠿⠟⠀⠀⠀⣿⣿⠀⠀⢿⣿⠀⠀⣿⣿⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⠀
    //⠘⣿⣧⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⢸⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⢿⣿⠀⣿⣿⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⠀
    //⠀⢿⣿⣄⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⢸⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⠀⢿⣿⣿⣿⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⠀
    //⠀⠀⠻⣿⣷⣦⣤⣤⣴⡄⠀⠀⣿⣿⣷⣶⣶⣶⣶⣶⠀⠀⠀⣿⣿⠀⠀⠀⢸⣿⣷⣶⣶⣶⣶⣶⡄⠀⠀⣿⣿⠀⠀⠀⠀⠀⣿⣿⣿⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⠀
    //⠀⠀⠀⠀⠉⠙⠛⠛⠉⠀⠀⠀⠈⠉⠉⠉⠉⠉⠉⠉⠀⠀⠀⠈⠉⠀⠀⠀⠀⠉⠉⠉⠉⠉⠉⠉⠀⠀⠀⠈⠉⠀⠀⠀⠀⠀⠀⠉⠁⠀⠀⠀⠀⠀⠀⠉⠁⠀⠀⠀⠀



    //endregion

    //region server
    //⠀⢀⣤⣶⣦⣤⣤⠀⠀⠀⠀⣤⣤⣤⣤⣤⣤⣤⠀⠀⠀⢠⣤⣤⣤⣤⣄⣀⠀⠀⠀⠀⣤⣄⠀⠀⠀⠀⠀⠀⢀⣤⠀⠀⠀⣤⣤⣤⣤⣤⣤⣤⠀⠀⠀⢠⣤⣤⣤⣤⣄⣀⠀⠀⠀
    //⣰⣿⡟⠉⠉⠙⠻⠃⠀⠀⢸⣿⡟⠛⠛⠛⠛⠛⠁⠀⠀⣿⣿⠛⠛⠛⠛⢿⣿⣆⠀⠀⢿⣿⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⢸⣿⡟⠛⠛⠛⠛⠛⠁⠀⠀⣿⣿⠛⠛⠛⠛⢿⣿⣆⠀
    //⣿⣿⠀⠀⠀⠀⠀⠀⠀⠀⢸⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⠀⠀⣿⣿⠀⠀⠈⣿⣷⠀⠀⠀⠀⢰⣿⡏⠀⠀⢸⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⠀⠀⣿⣿⠀
    //⠘⣿⣷⣄⠀⠀⠀⠀⠀⠀⢸⣿⣇⣀⣀⣀⣀⣀⠀⠀⠀⣿⣿⠀⠀⠀⠀⣠⣿⡟⠀⠀⠀⢻⣿⡄⠀⠀⠀⣿⣿⠀⠀⠀⢸⣿⣇⣀⣀⣀⣀⣀⠀⠀⠀⣿⣿⠀⠀⠀⠀⣠⣿⡟⠀
    //⠀⠀⠛⠿⣿⣿⣦⠀⠀⠀⢸⣿⡿⠿⠿⠿⠿⠟⠀⠀⠀⣿⣿⣿⣿⣿⣿⡿⠋⠀⠀⠀⠀⠀⣿⣷⠀⠀⢰⣿⠃⠀⠀⠀⢸⣿⡿⠿⠿⠿⠿⠟⠀⠀⠀⣿⣿⣿⣿⣿⣿⡿⠋⠀⠀
    //⠀⠀⠀⠀⠀⠙⣿⣿⠀⠀⢸⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⠻⣿⡄⠀⠀⠀⠀⠀⠹⣿⡄⠀⣿⡿⠀⠀⠀⠀⢸⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⠻⣿⡄⠀⠀
    //⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⢸⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⠀⢿⣿⠀⠀⠀⠀⠀⠀⣿⣿⢰⣿⠁⠀⠀⠀⠀⢸⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⠀⢿⣿⠀⠀
    //⢰⣷⣦⣤⣤⣾⣿⠟⠀⠀⢸⣿⣷⣶⣶⣶⣶⣶⡄⠀⠀⣿⣿⠀⠀⠀⠀⠈⣿⣷⠀⠀⠀⠀⠀⠘⣿⣿⡟⠀⠀⠀⠀⠀⢸⣿⣷⣶⣶⣶⣶⣶⡄⠀⠀⣿⣿⠀⠀⠀⠀⠈⣿⣷⠀
    //⠀⠈⠉⠛⠛⠉⠀⠀⠀⠀⠀⠉⠉⠉⠉⠉⠉⠉⠀⠀⠀⠈⠉⠀⠀⠀⠀⠀⠈⠉⠀⠀⠀⠀⠀⠀⠉⠉⠀⠀⠀⠀⠀⠀⠀⠉⠉⠉⠉⠉⠉⠉⠀⠀⠀⠈⠉⠀⠀⠀⠀⠀⠈⠉⠀

    @ConfigEntry.Category("Server")
    @ConfigEntry.Gui.Tooltip()
    private static boolean RECIPE_DISCOVERY = true;
    @ConfigEntry.Category("Server")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @ConfigEntry.Gui.Tooltip(count = 6)
    private static RECIPE_DISCOVERY_MODE_ENUM RECIPE_DISCOVERY_MODE = RECIPE_DISCOVERY_MODE_ENUM.INGREDIENT_AND_ITEM;

    //endregion

    //region common
    //⠀⢀⣶⣶⣷⣶⣄⠀⠀⠀⢀⣴⣶⣷⣶⣤⠀⠀⠀⢰⣶⣆⠀⠀⠀⠀⢰⣶⡆⠀⠀⣶⣶⠀⠀⠀⠀⠀⣶⣶⠀⠀⠀⢀⣴⣶⣷⣶⣤⠀⠀⠀⣶⣶⠀⠀⠀⠀⣶⡆
    //⢀⣿⡿⠁⠀⠙⣿⣇⠀⢠⣿⡿⠁⠀⠙⣿⣷⠀⠀⢸⣿⣿⠀⠀⠀⠀⣿⣿⡇⠀⠀⣿⣿⣇⠀⠀⠀⠀⣿⣿⠀⠀⢠⣿⡿⠁⠀⠙⣿⣷⠀⠀⣿⣿⣆⠀⠀⠀⣿⡇
    //⢸⣿⡇⠀⠀⠀⣿⣿⠀⢸⣿⡇⠀⠀⠀⣿⣿⠀⠀⢸⣿⣿⡄⠀⠀⠀⣿⣿⡇⠀⠀⣿⣿⣿⠀⠀⠀⢸⣿⣿⠀⠀⢸⣿⡇⠀⠀⠀⣿⣿⠀⠀⣿⣿⣿⠀⠀⠀⣿⡇
    //⢸⣿⡇⠀⠀⠀⠉⠉⠀⢸⣿⡇⠀⠀⠀⣿⣿⠀⠀⢸⣿⢹⣧⠀⠀⣸⡟⣿⡇⠀⠀⣿⣿⣿⡄⠀⠀⣿⠃⣿⠀⠀⢸⣿⡇⠀⠀⠀⣿⣿⠀⠀⣿⣿⢻⣿⠀⠀⣿⡇
    //⢸⣿⡇⠀⠀⠀⠀⠀⠀⢸⣿⡇⠀⠀⠀⣿⣿⠀⠀⢸⣿⠈⣿⠀⠀⣿⠃⣿⣷⠀⠀⣿⡏⢹⣧⠀⢀⣿⠀⣿⡆⠀⢸⣿⡇⠀⠀⠀⣿⣿⠀⠀⣿⣿⠀⣿⣧⠀⣿⡇
    //⢸⣿⡇⠀⠀⠀⣀⣀⠀⢸⣿⡇⠀⠀⠀⣿⣿⠀⠀⢸⣿⠀⣿⡆⢠⣿⠀⣿⣿⠀⠀⣿⡇⠀⣿⠀⣼⡏⠀⣿⡇⠀⢸⣿⡇⠀⠀⠀⣿⣿⠀⠀⣿⣿⠀⠈⣿⣄⣿⡇
    //⢸⣿⡇⠀⠀⠀⣿⣿⠀⢸⣿⡇⠀⠀⠀⣿⣿⠀⠀⣾⣿⠀⢸⣷⣼⡇⠀⣿⣿⠀⠀⣿⡇⠀⣿⡆⣿⠀⠀⣿⡇⠀⢸⣿⡇⠀⠀⠀⣿⣿⠀⠀⣿⣿⠀⠀⢹⣿⣿⡇
    //⠘⣿⣧⠀⠀⢀⣿⡏⠀⠘⣿⣧⠀⠀⢀⣿⣿⠀⠀⣿⣿⠀⠀⣿⣿⠀⠀⣿⣿⠀⠀⣿⡇⠀⠸⣿⣿⠀⠀⣿⡇⠀⠘⣿⣧⠀⠀⢀⣿⣿⠀⠀⣿⣿⠀⠀⠀⢿⣿⡇
    //⠀⠙⢿⣿⣿⣿⠟⠀⠀⠀⠙⢿⣿⣿⣿⠿⠁⠀⠀⣿⣿⠀⠀⢻⡿⠀⠀⣿⣿⠀⠀⣿⡇⠀⠀⣿⠇⠀⠀⣿⡇⠀⠀⠙⢿⣿⣿⣿⠿⠁⠀⠀⣿⣿⠀⠀⠀⠈⣿⡇

    @Override
    public boolean getModEnabled() {
        return false;
    }

    @Override
    public String getIngredientPrefix() {
        return "";
    }

    @Override
    public String getModidPrefix() {
        return "";
    }

    @Override
    public boolean getUseCustomUI() {
        return false;
    }

    @Override
    public boolean getDisplayHelpButton() {
        return false;
    }

    @Override
    public boolean getEnableRecipeBrowser() {
        return false;
    }

    @Override
    public boolean getRecipeDiscovery() {
        return false;
    }

    @Override
    public RECIPE_DISCOVERY_MODE_ENUM getRecipeDiscoveryMode() {
        return null;
    }

    //endregion
}
