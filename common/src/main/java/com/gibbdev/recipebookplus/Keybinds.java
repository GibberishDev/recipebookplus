package com.gibbdev.recipebookplus;

import com.gibbdev.recipebookplus.interfaces.IAbstractContainerScreen;
import com.gibbdev.recipebookplus.interfaces.IRecipeBookComponent;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
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
//                rbc.toggleVisibility();
//                TODO: write a method to toggle screens. rn it is divided between 3 classes: AbstractFurnaceScreen, CraftingScreen, InventoryScreen
            }
            if (!rbc.isVisible()) return; //unaccounted screens. prob from other mods not yet supported
            if (((IAbstractContainerScreen) screen).rbp$getSlotUnderCursor()==null || !((IAbstractContainerScreen) screen).rbp$getSlotUnderCursor().hasItem()) return;
            ItemStack hoveredItem = ((IAbstractContainerScreen) screen).rbp$getSlotUnderCursor().getItem();
            if (RECIPE_KEYBIND.matches(keyCode, scanCode)) {
                if ((keyModifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                    ((IRecipeBookComponent) rbc).rbp$search(BuiltInRegistries.ITEM.getKey(hoveredItem.getItem()).toString());
                } else {
                    ((IRecipeBookComponent) rbc).rbp$search(Component.translatable(hoveredItem.getDescriptionId()).getString());
                }
            } else
            if (USAGE_KEYBIND.matches(keyCode, scanCode)) {
                if ((keyModifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                    ((IRecipeBookComponent) rbc).rbp$search(Config.INSTANCE.getIngredientPrefix()+BuiltInRegistries.ITEM.getKey(hoveredItem.getItem()));
                } else {
                    ((IRecipeBookComponent) rbc).rbp$search(Config.INSTANCE.getIngredientPrefix()+Component.translatable(hoveredItem.getDescriptionId()).getString());
                }
            } else
            if (MOD_KEYBIND.matches(keyCode, scanCode)) {
                ((IRecipeBookComponent) rbc).rbp$search(Config.INSTANCE.getModidPrefix()+BuiltInRegistries.ITEM.getKey(hoveredItem.getItem()).getNamespace());
            }
//            ((IRecipeBookComponent) rbc).rbp$search(Component.translatable(.getDescriptionId()).getString());
        }
    }
}
