package com.gibbdev.recipebookplus;

import com.gibbdev.recipebookplus.interfaces.IAbstractContainerScreen;
import com.gibbdev.recipebookplus.interfaces.IRecipeBookComponent;
import com.gibbdev.recipebookplus.interfaces.accessors.IAbstractContainerScreenAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

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

    public static void KeybindEvent(Screen screen, int keyCode, int scanCode, int keyModifiers) {
        if (
            screen instanceof RecipeUpdateListener &&
            Minecraft.getInstance().player != null &&
            Config.getModEnabled()
        ) {
            if (!RECIPE_KEYBIND.matches(keyCode, scanCode) && !USAGE_KEYBIND.matches(keyCode, scanCode) && !MOD_KEYBIND.matches(keyCode, scanCode)) return;
            RecipeBookComponent rbc = ((RecipeUpdateListener) screen).getRecipeBookComponent();
            if (!rbc.isVisible()) {
                if (screen instanceof IAbstractContainerScreenAccessor) ((IAbstractContainerScreenAccessor) screen).recipebookplus$toggleRecipeBook();
            }
            if (!rbc.isVisible()) return; //unaccounted screens. prob from other mods not yet supported
            ItemStack hoveredItem = null;
            if (((IAbstractContainerScreen) screen).recipebookplus$getSlotUnderCursor()!=null && ((IAbstractContainerScreen) screen).recipebookplus$getSlotUnderCursor().hasItem()) hoveredItem = ((IAbstractContainerScreen) screen).recipebookplus$getSlotUnderCursor().getItem();
            int xPos = ((IAbstractContainerScreen) screen).recipebookplus$getMousePos().x;
            int yPos = ((IAbstractContainerScreen) screen).recipebookplus$getMousePos().y;
            if (((IRecipeBookComponent) rbc).recipebookplus$getGhostItemStack(xPos, yPos) != null) hoveredItem = ((IRecipeBookComponent) rbc).recipebookplus$getGhostItemStack(xPos, yPos);
            if (((IRecipeBookComponent) rbc).recipebookplus$getRecipeButtonDisplayItemStack(xPos,yPos) != null) hoveredItem = ((IRecipeBookComponent) rbc).recipebookplus$getRecipeButtonDisplayItemStack(xPos,yPos);
            if (hoveredItem == null) return;

            if (RECIPE_KEYBIND.matches(keyCode, scanCode)) {
                if ((keyModifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                    ((IRecipeBookComponent) rbc).recipebookplus$search(BuiltInRegistries.ITEM.getKey(hoveredItem.getItem()).toString());
                } else {
                    ((IRecipeBookComponent) rbc).recipebookplus$search(Component.translatable(hoveredItem.getDescriptionId()).getString());
                }
            } else
            if (USAGE_KEYBIND.matches(keyCode, scanCode)) {
                if ((keyModifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                    ((IRecipeBookComponent) rbc).recipebookplus$search(Config.getIngredientPrefix()+BuiltInRegistries.ITEM.getKey(hoveredItem.getItem()));
                } else {
                    ((IRecipeBookComponent) rbc).recipebookplus$search(Config.getIngredientPrefix()+Component.translatable(hoveredItem.getDescriptionId()).getString());
                }
            } else
            if (MOD_KEYBIND.matches(keyCode, scanCode)) {
                ((IRecipeBookComponent) rbc).recipebookplus$search(Config.getModidPrefix()+BuiltInRegistries.ITEM.getKey(hoveredItem.getItem()).getNamespace());
            }
//            ((IRecipeBookComponent) rbc).recipebookplus$search(Component.translatable(.getDescriptionId()).getString());
        }
    }
}
