package com.gibbdev.recipebookplus.commands;

import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.networking.ModStatus;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class RBPStatusCommand {

    public static void register(CommandDispatcher dispatcher) {
        dispatcher.register(Commands.literal("rbpstatus")
                .executes(ctx -> {
                        AbstractClientPlayer player = Minecraft.getInstance().player;
                        if (player == null) return 1;
                        if (Minecraft.getInstance().isSingleplayer()) {
                            player.sendSystemMessage(Component.literal("Running singleplayer world. Client config values applied."));
                        } else {
                            player.sendSystemMessage(Component.literal("Recipe Book+ status:"));
                            boolean installed = ModStatus.getInstalled();
                            player.sendSystemMessage(Component.literal("  Installed on server: ").append(installed ? "§2true§r - Version: " + ModStatus.getVersion() : "§4false§r - Using client config settings"));
                            if (installed) {
                                if (Config.getRecipeDiscovery()) {
                                    List<String> modes = new ArrayList<>();
                                    if (Config.getRecipeDiscoveryItem()) modes.add("ITEM");
                                    if (Config.getRecipeDiscoveryIngredient()) modes.add("INGREDIENT");
                                    if (Config.getRecipeDiscoveryAdvancement()) modes.add("ADVANCEMENT");
                                    if (modes.isEmpty()) player.sendSystemMessage(Component.literal(" Recipes cannot be discovered naturally"));
                                    else player.sendSystemMessage(Component.literal("Recipe discovery modes: "+modes));
                                } else {
                                    player.sendSystemMessage(Component.literal("  §6DISCOVERY§r IS OFF: All recipes are known"));
                                }

                            }
                        }
                        return 1;
                })
        );
    }

}
