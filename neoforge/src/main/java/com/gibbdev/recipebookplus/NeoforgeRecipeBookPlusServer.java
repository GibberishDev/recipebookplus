package com.gibbdev.recipebookplus;

import com.gibbdev.recipebookplus.platform.Services;
import com.gibbdev.recipebookplus.recipediscovery.RecipeDiscovery;
import com.gibbdev.recipebookplus.recipediscovery.RecipeDiscoverySavedData;
import com.gibbdev.recipebookplus.networking.RecipeDiscoveryPayloads;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.item.ItemEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod(value = Constants.MOD_ID, dist = Dist.DEDICATED_SERVER)
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.DEDICATED_SERVER)
public class NeoforgeRecipeBookPlusServer  {

    @SubscribeEvent
    public static void serverStarted(ServerStartedEvent event) {
        RecipeDiscovery.initServer(event.getServer());
    }

    @SubscribeEvent
    public static void clientLogin(PlayerEvent.PlayerLoggedInEvent event) {
        CompoundTag tag = RecipeDiscoverySavedData.getPlayerData(event.getEntity());
        if (tag != null) Services.PLATFORM.sendPayloadToClient((ServerPlayer) event.getEntity(), new RecipeDiscoveryPayloads.RDPlayerData(tag));
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Post event) {
        RecipeDiscovery.itemPickUp(event.getOriginalStack(), event.getPlayer().getUUID());
    }
}
