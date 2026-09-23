package com.gibbdev.recipebookplus;

import com.gibbdev.recipebookplus.commands.RBPStatusCommand;
import com.gibbdev.recipebookplus.networking.ModStatus;
import com.gibbdev.recipebookplus.networking.RecipeDiscoveryPayloads;
import com.gibbdev.recipebookplus.networking.ServerHandshakePayloads;
import com.gibbdev.recipebookplus.platform.Services;
import com.gibbdev.recipebookplus.recipediscovery.RecipeDiscovery;
import com.gibbdev.recipebookplus.ui.hud.HUDOverlays;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.client.ConfigScreenFactoryRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public class FabricRecipeBookPlusClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register(FabricRecipeBookPlusClient::onPlayerLogInEvent);
        ConfigScreenFactoryRegistry.INSTANCE.register(Constants.MOD_ID, ConfigurationScreen::new);
        HudRenderCallback.EVENT.register(((drawContext, tickCounter) -> HUDOverlays.RecipeAcquiredPopup.INSTANCE.render(Minecraft.getInstance(), Minecraft.getInstance().player, drawContext, Minecraft.getInstance().gui.getGuiTicks())));
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, a) -> RBPStatusCommand.register(dispatcher)
        );
        ClientPlayNetworking.registerGlobalReceiver(
                ServerHandshakePayloads.RESPONSE_TYPE,
                (payload, ctx) -> ModStatus.setVersion(payload.version())
        );
        ClientPlayNetworking.registerGlobalReceiver(
                RecipeDiscoveryPayloads.TYPE_RD_PLAYER_RECIPE_DATA,
                (payload, ctx) -> RecipeDiscovery.processPacket(payload.tag())
        );
        ClientPlayNetworking.registerGlobalReceiver(
                RecipeDiscoveryPayloads.TYPE_RD_GRANT_ONE_RECIPE,
                (payload, ctx) -> {
                    if (Minecraft.getInstance().player!=null) RecipeDiscovery.giveRecipe(payload.id(), Minecraft.getInstance().player.getUUID());
                }
        );
        ClientPlayNetworking.registerGlobalReceiver(
                RecipeDiscoveryPayloads.TYPE_RD_GRANT_RECIPE_LIST,
                (payload, ctx) -> {
                    if (Minecraft.getInstance().player!=null) RecipeDiscovery.giveRecipe(RecipeDiscoveryPayloads.getListFromTag(payload.tag()), Minecraft.getInstance().player.getUUID());
                }
        );
        ClientPlayNetworking.registerGlobalReceiver(
                RecipeDiscoveryPayloads.TYPE_RD_TAKE_ONE_RECIPE,
                (payload, ctx) -> {
                    if (Minecraft.getInstance().player!=null) RecipeDiscovery.takeRecipe(payload.id(), Minecraft.getInstance().player.getUUID());
                }
        );
        ClientPlayNetworking.registerGlobalReceiver(
                RecipeDiscoveryPayloads.TYPE_RD_TAKE_RECIPE_LIST,
                (payload, ctx) -> {
                    if (Minecraft.getInstance().player!=null) RecipeDiscovery.takeRecipe(RecipeDiscoveryPayloads.getListFromTag(payload.tag()), Minecraft.getInstance().player.getUUID());
                }
        );
        ClientPlayConnectionEvents.JOIN.register((a,b,c)-> Services.PLATFORM.getServerModVersion());
    }

    private static void onPlayerLogInEvent(ClientPacketListener clientPacketListener, PacketSender packetSender, Minecraft minecraft) {
        RecipeDiscovery.initClient();
    }

}
