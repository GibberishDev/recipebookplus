package com.gibbdev.recipebookplus;


import com.gibbdev.recipebookplus.commands.RBPRecipeCommand;
import com.gibbdev.recipebookplus.compat.FarmersDelight;
import com.gibbdev.recipebookplus.compat.FarmersDelightNeoforge;
import com.gibbdev.recipebookplus.recipediscovery.RecipeDiscovery;
import com.gibbdev.recipebookplus.networking.ModStatus;
import com.gibbdev.recipebookplus.networking.RecipeDiscoveryPayloads;
import com.gibbdev.recipebookplus.networking.ServerHandshakePayloads;
import com.gibbdev.recipebookplus.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Constants.MOD_ID)
@Mod(Constants.MOD_ID)
public class NeoforgeRecipeBookPlus {
    public NeoforgeRecipeBookPlus(IEventBus modEventBus, ModContainer modContainer) {
        FarmersDelight.INSTANCE = new FarmersDelightNeoforge();
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.Client.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.Server.SPEC);
        modEventBus.addListener(NeoforgeRecipeBookPlus::registerNetworking);
    }

    private static void registerNetworking(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Constants.MOD_ID);

        registrar.optional().playToServer(
                ServerHandshakePayloads.HANDSHAKE_TYPE,
                ServerHandshakePayloads.Handshake.CODEC,
                (payload, context) ->
                        context.reply(new ServerHandshakePayloads.HandshakeResponse(Services.PLATFORM.getModVersion()))
        );

        registrar.optional().playToClient(
                ServerHandshakePayloads.RESPONSE_TYPE,
                ServerHandshakePayloads.HandshakeResponse.CODEC,
                (payload,context)->{
                    ModStatus.setVersion(payload.version());
                }
        );

        registrar.optional().playToClient(
                RecipeDiscoveryPayloads.TYPE_RD_PLAYER_RECIPE_DATA,
                RecipeDiscoveryPayloads.RDPlayerData.CODEC,
                (payload,context)->{
                    RecipeDiscovery.processPacket(payload.tag());
                }
        );

        registrar.optional().playToClient(
                RecipeDiscoveryPayloads.TYPE_RD_GRANT_ONE_RECIPE,
                RecipeDiscoveryPayloads.RDGrantOneRecipe.CODEC,
                (payload,context)->{
                    if (Minecraft.getInstance().player == null) return;
                    RecipeDiscovery.giveRecipe(payload.id(), Minecraft.getInstance().player.getUUID());
                }
        );

        registrar.optional().playToClient(
                RecipeDiscoveryPayloads.TYPE_RD_GRANT_RECIPE_LIST,
                RecipeDiscoveryPayloads.RDGrantRecipeList.CODEC,
                (payload,context)->{
                    if (Minecraft.getInstance().player == null) return;
                    RecipeDiscovery.giveRecipe(RecipeDiscoveryPayloads.getListFromTag(payload.tag()), Minecraft.getInstance().player.getUUID());
                }
        );

        registrar.optional().playToClient(
                RecipeDiscoveryPayloads.TYPE_RD_TAKE_ONE_RECIPE,
                RecipeDiscoveryPayloads.RDTakeOneRecipe.CODEC,
                (payload,context)->{
                    if (Minecraft.getInstance().player == null) return;
                    RecipeDiscovery.takeRecipe(payload.id(), Minecraft.getInstance().player.getUUID());
                }
        );

        registrar.optional().playToClient(
                RecipeDiscoveryPayloads.TYPE_RD_TAKE_RECIPE_LIST,
                RecipeDiscoveryPayloads.RDTakeRecipeList.CODEC,
                (payload,context)->{
                    if (Minecraft.getInstance().player == null) return;
                    RecipeDiscovery.takeRecipe(RecipeDiscoveryPayloads.getListFromTag(payload.tag()), Minecraft.getInstance().player.getUUID());
                }
        );

    }
    @SubscribeEvent
    private static void registerCommands(RegisterCommandsEvent event) {
        RBPRecipeCommand.register(event.getDispatcher());
    }

}