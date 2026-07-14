package com.gibbdev.recipebookplus;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public class Keybinds {

    public static final String CATEGORY = "key.categories.recipebookplus";

    public static final KeyMapping RECIPE_KEYBIND = new KeyMapping(
            "recipebookplus.keymapping.recipe",
            KeyConflictContext.UNIVERSAL,
            KeyModifier.CONTROL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );
    public static final KeyMapping RECIPE_EXACT_KEYBIND = new KeyMapping(
            "recipebookplus.keymapping.recipe_exact",
            KeyConflictContext.UNIVERSAL,
            KeyModifier.ALT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );
    public static final KeyMapping USAGE_KEYBIND = new KeyMapping(
            "recipebookplus.keymapping.usage",
            KeyConflictContext.UNIVERSAL,
            KeyModifier.CONTROL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_U,
            CATEGORY
    );
    public static final KeyMapping USAGE_EXACT_KEYBIND = new KeyMapping(
            "recipebookplus.keymapping.usage_exact",
            KeyConflictContext.UNIVERSAL,
            KeyModifier.ALT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_U,
            CATEGORY
    );
    public static final KeyMapping MOD_KEYBIND = new KeyMapping(
            "recipebookplus.keymapping.mod",
            KeyConflictContext.UNIVERSAL,
            KeyModifier.CONTROL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            CATEGORY
    );

    public static boolean checkBind(KeyMapping bind, ScreenEvent.KeyPressed.Pre event) {
        boolean isShift = (event.getModifiers() & GLFW.GLFW_MOD_SHIFT) != 0;
        boolean isCtrl = (event.getModifiers() & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean isAlt = (event.getModifiers() & GLFW.GLFW_MOD_ALT) != 0;
        KeyModifier bindModifier = bind.getKeyModifier();
        if (bindModifier == KeyModifier.SHIFT && !isShift) return false;
        if (bindModifier == KeyModifier.CONTROL && !isCtrl) return false;
        if (bindModifier == KeyModifier.ALT && !isAlt) return false;
        return (bind.matches(event.getKeyCode(), event.getScanCode()));
    }
}
