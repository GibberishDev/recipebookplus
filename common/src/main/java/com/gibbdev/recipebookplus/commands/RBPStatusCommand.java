package com.gibbdev.recipebookplus.commands;

import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.networking.ModStatus;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

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
                                    switch (Config.getRecipeDiscoveryMode()) {
                                        case NONE ->
                                                player.sendSystemMessage(Component.literal("  §6NONE§r: no recipe discovery. Recipes are granted by something else"));
                                        case ITEM ->
                                                player.sendSystemMessage(Component.literal("  §6ITEM§r: recipe for item is granted when that item is obtained"));
                                        case INGREDIENT ->
                                                player.sendSystemMessage(Component.literal("  §6INGREDIENT§r: recipe for item is granted when any ingredient is obtained"));
                                        case INGREDIENT_AND_ITEM ->
                                                player.sendSystemMessage(Component.literal("  §6INGREDIENT_AND_ITEM§r: recipe for item is granted when any ingredient or item itself is obtained"));
                                        case ADVANCEMENT ->
                                                player.sendSystemMessage(Component.literal("  §6ADVANCEMENT§r: Default minecraft recipe discovery"));
                                    }
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
