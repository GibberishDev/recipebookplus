package com.gibbdev.recipebookplus;

import com.gibbdev.recipebookplus.commands.RBPStatusCommand;
import com.gibbdev.recipebookplus.platform.Services;
import com.gibbdev.recipebookplus.recipediscovery.RecipeDiscovery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class NeoforgeRecipeBookPlusClient {
    public NeoforgeRecipeBookPlusClient(IEventBus ignoredModEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }


    @SubscribeEvent
    public static void onKeybindRegister(RegisterKeyMappingsEvent event) {
        event.register(Keybinds.RECIPE_KEYBIND);
        event.register(Keybinds.USAGE_KEYBIND);
        event.register(Keybinds.MOD_KEYBIND);
    }
    @SubscribeEvent
    public static void onKeybind(ScreenEvent.KeyPressed.Pre event) {
        if (!Config.getModEnabled()) return;
        Keybinds.KeybindEvent(event.getScreen(), event.getKeyCode(), event.getScanCode(), event.getModifiers());
    }

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        RBPStatusCommand.register(event.getDispatcher());
        event.getDispatcher().register(Commands.literal("rbptest").executes((ctx)->RecipeDiscovery.giveRecipe("minecraft:stonecutter", Minecraft.getInstance().player.getUUID())?1:0));
    }

    @SubscribeEvent
    public static void clientLogIn(ClientPlayerNetworkEvent.LoggingIn event) {
        Services.PLATFORM.getServerModVersion();
        RecipeDiscovery.initClient();
    }
    @SubscribeEvent
    public static void clientLogOut(ClientPlayerNetworkEvent.LoggingOut event) {
        RecipeDiscovery.resetClient();
    }
}