package com.gibbdev.recipebookplus;

import com.gibbdev.recipebookplus.commands.RBPRecipeCommand;
import com.gibbdev.recipebookplus.compat.FarmersDelight;
import com.gibbdev.recipebookplus.compat.FarmersDelightRefabricated;
import com.gibbdev.recipebookplus.networking.ModStatus;
import com.gibbdev.recipebookplus.networking.RecipeDiscoveryPayloads;
import com.gibbdev.recipebookplus.networking.ServerHandshakePayloads;
import com.gibbdev.recipebookplus.platform.FabricPlatformHelper;
import com.gibbdev.recipebookplus.platform.Services;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.neoforged.fml.config.ModConfig;

public class FabricRecipeBook implements ModInitializer {
    
    @Override
    public void onInitialize() {
        FarmersDelight.INSTANCE = new FarmersDelightRefabricated();
        NeoForgeConfigRegistry.INSTANCE.register(Constants.MOD_ID, ModConfig.Type.SERVER, Config.Server.SPEC);
        NeoForgeConfigRegistry.INSTANCE.register(Constants.MOD_ID, ModConfig.Type.CLIENT, Config.Client.SPEC);
        CommandRegistrationCallback.EVENT.register(((dispatcher, a, b) -> RBPRecipeCommand.register(dispatcher)));
        registerNetworking();
    }

    private void registerNetworking() {
        PayloadTypeRegistry.playC2S().register(ServerHandshakePayloads.HANDSHAKE_TYPE, ServerHandshakePayloads.Handshake.CODEC);
        PayloadTypeRegistry.playS2C().register(ServerHandshakePayloads.RESPONSE_TYPE, ServerHandshakePayloads.HandshakeResponse.CODEC);
        PayloadTypeRegistry.playS2C().register(RecipeDiscoveryPayloads.TYPE_RD_PLAYER_RECIPE_DATA, RecipeDiscoveryPayloads.RDPlayerData.CODEC);
        PayloadTypeRegistry.playS2C().register(RecipeDiscoveryPayloads.TYPE_RD_GRANT_ONE_RECIPE, RecipeDiscoveryPayloads.RDGrantOneRecipe.CODEC);
        PayloadTypeRegistry.playS2C().register(RecipeDiscoveryPayloads.TYPE_RD_GRANT_RECIPE_LIST, RecipeDiscoveryPayloads.RDGrantRecipeList.CODEC);
        PayloadTypeRegistry.playS2C().register(RecipeDiscoveryPayloads.TYPE_RD_TAKE_ONE_RECIPE, RecipeDiscoveryPayloads.RDTakeOneRecipe.CODEC);
        PayloadTypeRegistry.playS2C().register(RecipeDiscoveryPayloads.TYPE_RD_TAKE_RECIPE_LIST, RecipeDiscoveryPayloads.RDTakeRecipeList.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ServerHandshakePayloads.HANDSHAKE_TYPE, (payload, ctx) ->{
            ServerPlayNetworking.send(
                    ctx.player(),
                    new ServerHandshakePayloads.HandshakeResponse(Services.PLATFORM.getModVersion())
            );
        });
    }


}
