package com.gibbdev.recipebookplus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = RecipeBookPlus.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = RecipeBookPlus.MODID, value = Dist.CLIENT)
public class RecipeBookPlusClient {
    public RecipeBookPlusClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
    }

    @SubscribeEvent
    static void onKeybindRegister(RegisterKeyMappingsEvent event) {
        event.register(Keybinds.RECIPE_KEYBIND);
        event.register(Keybinds.RECIPE_EXACT_KEYBIND);
        event.register(Keybinds.USAGE_KEYBIND);
        event.register(Keybinds.USAGE_EXACT_KEYBIND);
        event.register(Keybinds.MOD_KEYBIND);
    }

    @SubscribeEvent
    static void onKeybind(ScreenEvent.KeyPressed.Pre event) {
        if (!Config.MOD_ENABLED.get()) return;
        Minecraft mc = Minecraft.getInstance();
        if (event.getScreen() instanceof AbstractContainerScreen screen) {
            if (screen.getSlotUnderMouse() == null) return;
            if (!screen.getSlotUnderMouse().hasItem()) return;
            Item item = screen.getSlotUnderMouse().getItem().getItem();
            String itemName = Component.translatable(item.getDescriptionId()).getString();
            String itemId = BuiltInRegistries.ITEM.getKey(item).toString();
            String namespace = BuiltInRegistries.ITEM.getKey(item).getNamespace();
            RecipeBookComponent rbc = null;
            switch (event.getScreen()) {
                case InventoryScreen scr -> rbc = scr.recipeBookComponent;
                case CraftingScreen scr -> rbc = scr.recipeBookComponent;
                case AbstractFurnaceScreen scr -> rbc = scr.recipeBookComponent;
                default -> {
                    return;
                }
            }
            if (!rbc.isVisible()) return;
            if (Keybinds.checkBind(Keybinds.RECIPE_KEYBIND, event)) {rbc.searchBox.setValue(itemName);}
            if (Keybinds.checkBind(Keybinds.RECIPE_EXACT_KEYBIND, event)) {rbc.searchBox.setValue(itemId);}
            if (Keybinds.checkBind(Keybinds.USAGE_KEYBIND, event)) {rbc.searchBox.setValue(Config.INGREDIENT_PREFIX.get()+itemName);}
            if (Keybinds.checkBind(Keybinds.USAGE_EXACT_KEYBIND, event)) {rbc.searchBox.setValue(Config.INGREDIENT_PREFIX.get()+itemId);}
            if (Keybinds.checkBind(Keybinds.MOD_KEYBIND, event)) {rbc.searchBox.setValue(Config.MODID_PREFIX.get()+namespace);}
            if (rbc.searchBox.getValue().isEmpty()) return;
            rbc.updateCollections(true);
            rbc.searchBox.moveCursorToStart(false);
            event.setCanceled(true);
        }
    }

}
