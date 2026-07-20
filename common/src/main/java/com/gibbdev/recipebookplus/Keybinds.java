package com.gibbdev.recipebookplus;

import com.gibbdev.recipebookplus.interfaces.IAbstractRecipeBookScreenMixin;
import com.gibbdev.recipebookplus.interfaces.IRecipeBookComponentMixin;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class Keybinds {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(Constants.MOD_ID,"category"));

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

    public static void KeybindEvent(Screen screen, KeyEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.getConnection() == null) return;
        if (!(screen instanceof IAbstractRecipeBookScreenMixin)) return;
        if (!RECIPE_KEYBIND.matches(event) && !USAGE_KEYBIND.matches(event) && !MOD_KEYBIND.matches(event)) return;
        RecipeBookComponent<?> rbc = ((IAbstractRecipeBookScreenMixin) screen).rbp$getRecipeBookComponent();
        if (!rbc.isVisible()) return;
        EditBox searchBox = null;
        if (rbc instanceof IRecipeBookComponentMixin) searchBox = ((IRecipeBookComponentMixin) rbc).rbp$getSearchBox();
        if (searchBox == null) return;
        ItemStack item = ((IAbstractRecipeBookScreenMixin) screen).rbp$getSlotUnderCursor();
        if (item == null) return;
        searchBox.setFocused(false);
        if (RECIPE_KEYBIND.matches(event)) {
            if (event.hasControlDown()) {
                searchBox.setValue(BuiltInRegistries.ITEM.getKey(item.getItem()).toString());
            } else {
                searchBox.setValue(Component.translatable(item.getItem().getDescriptionId()).getString());
            }
        } else if (USAGE_KEYBIND.matches(event)) {
            if (event.hasControlDown()) {
                searchBox.setValue(Config.INSTANCE.getIngredientPrefix()+BuiltInRegistries.ITEM.getKey(item.getItem()));
            } else {
                searchBox.setValue(Config.INSTANCE.getIngredientPrefix()+Component.translatable(item.getItem().getDescriptionId()).getString());
            }
        } else if (MOD_KEYBIND.matches(event)) {
            searchBox.setValue(Config.INSTANCE.getModidPrefix()+BuiltInRegistries.ITEM.getKey(item.getItem()).getNamespace());
        } else {
            return;
        }
        if (searchBox.getValue().isEmpty()) return;
        searchBox.moveCursorToStart(false);
        ((IRecipeBookComponentMixin) rbc).rbp$resetSearch();
    }
}
