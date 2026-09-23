package com.gibbdev.recipebookplus.commands;

import com.gibbdev.recipebookplus.recipediscovery.RecipeDiscovery;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.*;

public class RBPRecipeCommand {
    public RBPRecipeCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rbprecipe")
                .requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                .then(Commands.literal("give")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("recipe", ResourceLocationArgument.id())
                                        .suggests(SuggestionProviders.ALL_RECIPES)
                                        .executes((ctx)->grantRecipes(
                                                ctx.getSource(),
                                                EntityArgument.getPlayers(ctx, "targets"),
                                                Collections.singleton(ResourceLocationArgument.getRecipe(ctx, "recipe"))
                                        ))
                                )
                                .then(Commands.literal("*")
                                        .executes((ctx)->grantRecipes(
                                                ctx.getSource(),
                                                EntityArgument.getPlayers(ctx, "targets"),
                                                (ctx.getSource()).getServer().getRecipeManager().getRecipes()
                                        ))
                                )
                        )
                )
                .then(Commands.literal("take")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("recipe", ResourceLocationArgument.id())
                                        .suggests(SuggestionProviders.ALL_RECIPES)
                                        .executes((ctx)->takeRecipes(
                                                ctx.getSource(),
                                                EntityArgument.getPlayers(ctx, "targets"),
                                                Collections.singleton(ResourceLocationArgument.getRecipe(ctx, "recipe"))
                                        ))
                                )
                                .then(Commands.literal("*")
                                        .executes((ctx)->takeRecipes(
                                                ctx.getSource(),
                                                EntityArgument.getPlayers(ctx, "targets"),
                                                (ctx.getSource()).getServer().getRecipeManager().getRecipes()
                                        ))
                                )
                        )
                )
        );
    }

    private static int grantRecipes(CommandSourceStack source, Collection<ServerPlayer> targets, Collection<RecipeHolder<?>> recipes) {
        for (ServerPlayer player : targets) {
            if (recipes.size() == 1) {
                Optional<RecipeHolder<?>> holder = recipes.stream().findFirst();
                if (RecipeDiscovery.giveRecipe(holder.get().id().toString(), player.getUUID())) {
                    source.sendSystemMessage(Component.translatable("command.feedback.success.grant_one_recipe", holder.get().id().toString(), player.getDisplayName()));
                } else player.sendSystemMessage(Component.translatable("command.feedback.fail.grant_one_recipe", player.getDisplayName()).withStyle(ChatFormatting.RED));
            } else {
                List<String> ids = new ArrayList<>();
                recipes.forEach(recipeHolder -> ids.add(recipeHolder.id().toString()));
                List<String> added = RecipeDiscovery.giveRecipe(ids, player.getUUID());
                if (!added.isEmpty()) {
                    source.sendSystemMessage(Component.translatable("command.feedback.success.grant_many_recipes", Integer.toString(added.size()), player.getDisplayName()));
                } else player.sendSystemMessage(Component.translatable("command.feedback.fail.grant_one_recipe", player.getDisplayName()).withStyle(ChatFormatting.RED));
            }
        }
        return 1;
    }
    private static int takeRecipes(CommandSourceStack source, Collection<ServerPlayer> targets, Collection<RecipeHolder<?>> recipes) {
        for (ServerPlayer player : targets) {
            if (recipes.size() == 1) {
                Optional<RecipeHolder<?>> holder = recipes.stream().findFirst();
                if (RecipeDiscovery.takeRecipe(holder.get().id().toString(), player.getUUID())) {
                    source.sendSystemMessage(Component.translatable("command.feedback.success.take_one_recipe", holder.get().id().toString(), player.getDisplayName()));
                } else player.sendSystemMessage(Component.translatable("command.feedback.fail.take_one_recipe", player.getDisplayName()).withStyle(ChatFormatting.RED));
            } else {
                List<String> ids = new ArrayList<>();
                recipes.forEach(recipeHolder -> ids.add(recipeHolder.id().toString()));
                List<String> taken = RecipeDiscovery.takeRecipe(ids, player.getUUID());
                if (!taken.isEmpty()) {
                    source.sendSystemMessage(Component.translatable("command.feedback.success.take_many_recipes", Integer.toString(taken.size()), player.getDisplayName()));
                } else player.sendSystemMessage(Component.translatable("command.feedback.fail.take_one_recipe", player.getDisplayName()).withStyle(ChatFormatting.RED));
            }
        }
        return 1;
    }
}
