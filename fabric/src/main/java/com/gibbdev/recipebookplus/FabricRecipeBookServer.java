package com.gibbdev.recipebookplus;

import com.gibbdev.recipebookplus.networking.RecipeDiscoveryPayloads;
import com.gibbdev.recipebookplus.platform.Services;
import com.gibbdev.recipebookplus.recipediscovery.RecipeDiscovery;
import com.gibbdev.recipebookplus.recipediscovery.RecipeDiscoverySavedData;
import com.gibbdev.recipebookplus.recipediscovery.RecipeLookup;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.level.ServerPlayer;

public class FabricRecipeBookServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTED.register(RecipeDiscovery::initServer);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server,a,success)->{if (success) RecipeLookup.rebuild(server.getRecipeManager(), server.registryAccess());});
        ServerPlayerEvents.JOIN.register(FabricRecipeBookServer::clientLogInEvent);
    }

    private static void clientLogInEvent(ServerPlayer player) {
        Services.PLATFORM.sendPayloadToClient(player, new RecipeDiscoveryPayloads.RDPlayerData(RecipeDiscoverySavedData.getPlayerData(player)));
    }

}
