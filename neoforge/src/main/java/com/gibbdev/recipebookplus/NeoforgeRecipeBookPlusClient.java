package com.gibbdev.recipebookplus;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class NeoforgeRecipeBookPlusClient {
    public NeoforgeRecipeBookPlusClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void registerKeybinds(RegisterKeyMappingsEvent e) {
        e.registerCategory(Keybinds.CATEGORY);
        for (KeyMapping key : Keybinds.getKeybinds()) {
            e.register(key);
        }
    }

    @SubscribeEvent
    public static void screenKeypress(ScreenEvent.KeyPressed.Pre event) {
        if (!Config.INSTANCE.getModEnabled()) return;
        Keybinds.KeybindEvent(event.getScreen(), event.getKeyEvent());
        event.setCanceled(false);
    }
}