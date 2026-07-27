package com.gibbdev.recipebookplus;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Keybinds {
    public static final String CATEGORY = "key.category.recipebookplus.category";

    public static final KeyMapping RECIPE_KEYBIND = new KeyMapping(
            "recipebookplus.keymapping.recipe",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );
    public static final KeyMapping USAGE_KEYBIND = new KeyMapping(
            "recipebookplus.keymapping.usage",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_U,
            CATEGORY
    );
    public static final KeyMapping MOD_KEYBIND = new KeyMapping(
            "recipebookplus.keymapping.mod",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            CATEGORY
    );

    public static List<KeyMapping> getKeybinds() {
        List<KeyMapping> keybinds = new ArrayList<>();
        keybinds.add(RECIPE_KEYBIND);
        keybinds.add(USAGE_KEYBIND);
        keybinds.add(MOD_KEYBIND);
        return keybinds;
    }
    public static void KeybindEvent(Screen screen, int keyCode, int scanCode, int keyModifiers) {
        if (
            screen instanceof RecipeUpdateListener &&
            Minecraft.getInstance().player != null &&
            Config.INSTANCE.getModEnabled()
        ) {
            if (!RECIPE_KEYBIND.matches(keyCode, scanCode) && !USAGE_KEYBIND.matches(keyCode, scanCode) && !MOD_KEYBIND.matches(keyCode, scanCode)) return;
            RecipeBookComponent rbc = ((RecipeUpdateListener) screen).getRecipeBookComponent();
            if (!rbc.isVisible()) {
                rbc.toggleVisibility();
            }
        }
    }
}
