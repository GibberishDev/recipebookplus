package com.gibbdev.recipebookplus;

import com.gibbdev.recipebookplus.commands.RBPStatusCommand;
import com.gibbdev.recipebookplus.platform.Services;
import com.gibbdev.recipebookplus.recipediscovery.RecipeDiscovery;
import com.gibbdev.recipebookplus.recipediscovery.RecipeLookup;
import com.gibbdev.recipebookplus.ui.hud.HUDOverlays;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class NeoforgeRecipeBookPlusClient {
    public NeoforgeRecipeBookPlusClient(IEventBus ignoredModEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }


    @SubscribeEvent
    public static void recipebookplus$onKeybindRegister(RegisterKeyMappingsEvent event) {
        event.register(Keybinds.RECIPE_KEYBIND);
        event.register(Keybinds.USAGE_KEYBIND);
        event.register(Keybinds.MOD_KEYBIND);
    }
    @SubscribeEvent
    public static void recipebookplus$onKeybind(ScreenEvent.KeyPressed.Pre event) {
        if (!Config.getModEnabled()) return;
        Keybinds.KeybindEvent(event.getScreen(), event.getKeyCode(), event.getScanCode(), event.getModifiers());
    }

    @SubscribeEvent
    public static void recipebookplus$onRegisterClientCommands(RegisterClientCommandsEvent event) {
        RBPStatusCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void recipebookplus$onClientLogIn(ClientPlayerNetworkEvent.LoggingIn event) {
        Services.PLATFORM.getServerModVersion();
        RecipeDiscovery.initClient();
    }
    @SubscribeEvent
    public static void recipebookplus$onClientLogOut(ClientPlayerNetworkEvent.LoggingOut event) {
        RecipeDiscovery.save();
        RecipeDiscovery.resetClient();
    }

    @SubscribeEvent
    public static void recipebookplus$onRecipesUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            if (Minecraft.getInstance().level==null || !Minecraft.getInstance().isSingleplayer())return;
            RecipeLookup.rebuild(Minecraft.getInstance().level.getRecipeManager(), Minecraft.getInstance().level.registryAccess());
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        HUDOverlays.RecipeAcquiredPopup.INSTANCE.render(Minecraft.getInstance(), Minecraft.getInstance().player, event.getGuiGraphics(), Minecraft.getInstance().gui.getGuiTicks());
    }
}