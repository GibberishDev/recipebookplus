package com.gibbdev.recipebookplus;

import com.gibbdev.recipebookplus.platform.Services;
import com.gibbdev.recipebookplus.recipediscovery.RecipeDiscovery;
import com.gibbdev.recipebookplus.recipediscovery.RecipeDiscoverySavedData;
import com.gibbdev.recipebookplus.networking.RecipeDiscoveryPayloads;
import com.gibbdev.recipebookplus.recipediscovery.RecipeLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@Mod(value = Constants.MOD_ID, dist = Dist.DEDICATED_SERVER)
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.DEDICATED_SERVER)
public class NeoforgeRecipeBookPlusServer  {

    @SubscribeEvent
    public static void recipebookplus$onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        RecipeDiscovery.initServer(server);
        RecipeLookup.rebuild(server.getRecipeManager(), server.registryAccess());
    }

    @SubscribeEvent
    public static void recipebookplus$onClientLogin(PlayerEvent.PlayerLoggedInEvent event) {
        CompoundTag tag = RecipeDiscoverySavedData.getPlayerData(event.getEntity());
        Services.PLATFORM.sendPayloadToClient((ServerPlayer) event.getEntity(), new RecipeDiscoveryPayloads.RDPlayerData(tag));
    }

    @SubscribeEvent
    public static void recipebookplus$onRecipesUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server==null)return;
            RecipeLookup.rebuild(server.getRecipeManager(), server.registryAccess());
        }
    }
}
