package com.gibbdev.recipebookplus.interfaces;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;
import java.util.UUID;

public interface IRecipeDiscovery {
    List<RecipeHolder<?>> getKnownHolders(UUID uuid);
    boolean hasAll(List<String> ids, UUID uuid);
    boolean isKnown(String id, UUID uuid);
    boolean giveRecipe(String id, UUID uuid);
    List<String> giveRecipe(List<String> ids, UUID uuid);
    boolean takeRecipe(String id, UUID uuid);
    List<String> takeRecipe(List<String> ids, UUID uuid);
}
