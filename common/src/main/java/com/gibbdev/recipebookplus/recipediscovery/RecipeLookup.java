package com.gibbdev.recipebookplus.recipediscovery;

import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.Constants;
import com.gibbdev.recipebookplus.compat.FarmersDelight;
import com.gibbdev.recipebookplus.platform.Services;
import com.gibbdev.recipebookplus.ui.hud.HUDOverlays;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.*;

public class RecipeLookup {
    private static final Map<ResourceLocation, Set<ResourceLocation>> itemLookupMap = new HashMap<>();
    private static final Map<ResourceLocation, Set<ResourceLocation>> ingredientLookupMap = new HashMap<>();

    public static Set<ResourceLocation> searchByItem(ResourceLocation item) {
        if (itemLookupMap.containsKey(item)) return itemLookupMap.get(item);
        return new HashSet<>();
    }
    public static Set<ResourceLocation> searchByIngredient(ResourceLocation item) {
        if (ingredientLookupMap.containsKey(item)) return ingredientLookupMap.get(item);
        return new HashSet<>();
    }

    public static Set<ResourceLocation> search(ResourceLocation item) {
        Set<ResourceLocation> ids = new HashSet<>();
        if (Config.getRecipeDiscoveryItem()) ids.addAll(searchByItem(item));
        if (Config.getRecipeDiscoveryIngredient()) ids.addAll(searchByIngredient(item));
        return ids;
    }

    public static void rebuild(RecipeManager recipeManager, RegistryAccess ra) {
        itemLookupMap.clear();
        ingredientLookupMap.clear();
        List<RecipeHolder<?>> recipes = recipeManager.getRecipes().stream().toList();
        for (RecipeHolder<?> holder : recipes) {
            ResourceLocation id = holder.id();
            holder.value().getIngredients().forEach(ing -> Arrays.stream(ing.getItems()).forEach(itemStack -> {
                ResourceLocation itemID = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
                if (ingredientLookupMap.containsKey(itemID)) {
                    Set<ResourceLocation> ids = new HashSet<>(ingredientLookupMap.get(itemID));
                    ids.add(id);
                    ingredientLookupMap.put(itemID,ids);
                } else {
                    ingredientLookupMap.put(itemID,Set.of(id));
                }
            }));
            if (Services.PLATFORM.isModLoaded("farmersdelight")) {
                if (FarmersDelight.getRecipeContainer(holder) != ItemStack.EMPTY) {
                    ResourceLocation itemID = BuiltInRegistries.ITEM.getKey(FarmersDelight.getRecipeContainer(holder).getItem());
                    if (ingredientLookupMap.containsKey(itemID)) {
                        Set<ResourceLocation> ids = new HashSet<>(ingredientLookupMap.get(itemID));
                        ids.add(id);
                        ingredientLookupMap.put(itemID, ids);
                    } else {
                        ingredientLookupMap.put(itemID, Set.of(id));
                    }
                }
            }
            ResourceLocation itemID = BuiltInRegistries.ITEM.getKey(holder.value().getResultItem(ra).getItem());
            if (itemLookupMap.containsKey(itemID)) {
                Set<ResourceLocation> ids = new HashSet<>(itemLookupMap.get(itemID));
                ids.add(id);
                itemLookupMap.put(itemID,ids);
            } else {
                itemLookupMap.put(itemID,Set.of(id));
            }
        }
        Constants.LOG.info("Rebuilt recipe lookup maps");
    }

    public static void checkPickedUpItems(ItemStack itemStack, Player player) {
        Set<ResourceLocation> ids = search(BuiltInRegistries.ITEM.getKey(itemStack.getItem()));
        List<String> idsSTR = new ArrayList<>();
        ids.forEach(rl -> idsSTR.add(rl.toString()));
        if (!RecipeDiscovery.hasAll(idsSTR, player.getUUID())) {
            List<String> awardedRecipes = new ArrayList<>(RecipeDiscovery.giveRecipe(idsSTR, player.getUUID()));
            if (Config.getRecipeDiscoveryAdvancement() && player instanceof LocalPlayer && Config.getRecipeOverlayAnchor()!= Config.HUDOverlayAnchor.NONE) {
                awardedRecipes.removeIf(id -> {
                    ClientRecipeBook rb = ((LocalPlayer) player).getRecipeBook();
                    return rb.contains(ResourceLocation.parse(id));
                });
                HUDOverlays.RecipeAcquiredPopup.queueRecipeNotification(Set.copyOf(awardedRecipes));
            }
        }
    }

}
