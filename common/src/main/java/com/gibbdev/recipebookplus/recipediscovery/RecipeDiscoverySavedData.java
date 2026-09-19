package com.gibbdev.recipebookplus.recipediscovery;

import com.gibbdev.recipebookplus.Constants;
import com.gibbdev.recipebookplus.interfaces.IRecipeDiscovery;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Class that manages save data for known recipes of players in current server.
 */
public class RecipeDiscoverySavedData extends SavedData implements IRecipeDiscovery {
    /**
     * Dictionary of players with knowledge book data. In case of single player or no server mod installed should contain only one player associated with current client
     */
    private static final Map<String, Set<String>> knownRecipes = new HashMap<>();

    /**
     * @param level Server level to load persistent data from
     * @return returns object of the player recipe knowledge data
     */
    public static RecipeDiscoverySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(
                        RecipeDiscoverySavedData::new,
                        RecipeDiscoverySavedData::load,
                        null
                ),
                "recipe_discovery"
        );
    }

    /**
     * Saves compoundTag data to persistent storage
     */
    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag, HolderLookup.@NotNull Provider provider) {
        CompoundTag recipeDiscovery = new CompoundTag();
        for (Map.Entry<String, Set<String>> entry : knownRecipes.entrySet()) {
            ListTag recipes = new ListTag();
            entry.getValue().stream().map(StringTag::valueOf).forEach(recipes::add);
            recipeDiscovery.put(entry.getKey(), recipes);
        }
        compoundTag.put("recipeDiscovery", recipeDiscovery);
        return compoundTag;
    }

    /**
     * loads compoundTag data from persistent storage
     */
    public static RecipeDiscoverySavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        RecipeDiscoverySavedData data = new RecipeDiscoverySavedData();
        CompoundTag loadedData = tag.getCompound("recipeDiscovery");
        for (String uuidString : loadedData.getAllKeys()) {
            ListTag recipesListTag = loadedData.getList(uuidString, StringTag.TAG_STRING);
            Set<String> recipes = new HashSet<>();
            recipesListTag.forEach((s)->recipes.add(s.getAsString()));
            data.knownRecipes.put(uuidString, recipes);
        }
        Constants.LOG.info("Loaded recipe discovery data for "+loadedData.getAllKeys().size()+" players");
        return data;
    }

    /**
     * @param uuid Targeted player
     * @return List of RecipeHolders that player knows
     */
    @Override
    public List<RecipeHolder<?>> getKnownHolders(UUID uuid) {
        List<RecipeHolder<?>> list = new ArrayList<>();
        if (Minecraft.getInstance().level == null) return list;
        if (!knownRecipes.containsKey(uuid.toString())) return list;
        for (String id : knownRecipes.get(uuid.toString())) {
            Optional<RecipeHolder<?>> holder = Minecraft.getInstance().level.getRecipeManager().byKey(ResourceLocation.parse(id));
            if (holder.isEmpty()) continue;
            list.add(holder.get());
        }
        return list;
    }

    /**
     * @param ids List of recipe IDs to check
     * @param uuid Targeted player
     * @return Whether player knows all recipes inside the list
     */
    @Override
    public boolean hasAll(List<String> ids, UUID uuid) {
        if (!knownRecipes.containsKey(uuid.toString())) return false;
        return knownRecipes.get(uuid.toString()).containsAll(ids);
    }

    /**
     * @param id Recipe ID
     * @param uuid Targeted player
     * @return Whether recipe is known by player
     */
    @Override
    public boolean isKnown(String id, UUID uuid) {
        if (!knownRecipes.containsKey(uuid.toString())) return false;
        return knownRecipes.get(uuid.toString()).contains(id);
    }


    /**
     * Award specific recipe ID to player's knowledge book
     * @param id Recipe ID that needs to be given
     * @param uuid Targeted player
     * @return Boolean whether recipe ID was actually given. Meaning if this value is False player already knew that recipe
     */
    @Override
    public boolean giveRecipe(String id, UUID uuid) {
        if (!isKnown(id, uuid)) {
            if (!knownRecipes.containsKey(uuid.toString())) knownRecipes.put(uuid.toString(), new HashSet<>());
            knownRecipes.get(uuid.toString()).add(id);
            setDirty();
            return true;
        }
        return false;
    }

    /**
     * Award all possible recipe IDs in provided list to player's knowledge book
     * @param ids List of recipe IDs that should be given
     * @param uuid Targeted player
     * @return List of recipe IDs that was actually given. Meaning this list excludes already known recipe IDs
     */
    @Override
    public List<String> giveRecipe(List<String> ids, UUID uuid) {
        List<String> actuallyAwarded = new ArrayList<>();
        for (String id : ids) if (giveRecipe(id, uuid)) actuallyAwarded.add(id);
        return actuallyAwarded;
    }

    /**
     * Remove specific recipe ID from player's knowledge book
     * @param id Recipe ID that needs to be taken away
     * @param uuid Targeted player
     * @return Boolean whether recipe ID was actually taken away. Meaning if this value is False player already wasn't aware of that recipe
     */
    @Override
    public boolean takeRecipe(String id, UUID uuid) {
        if (isKnown(id, uuid)) {
            if (!knownRecipes.containsKey(uuid.toString())) return false;
            knownRecipes.get(uuid.toString()).remove(id);
            setDirty();
            return true;
        }
        return false;
    }

    /**
     * Remove all possible recipe IDs in provided list from player's knowledge book
     * @param ids List of recipe IDs that should be taken away
     * @param uuid Targeted player
     * @return List of recipe IDs that was actually taken away. Meaning this list excludes already unknown recipe IDs
     */
    @Override
    public List<String> takeRecipe(List<String> ids, UUID uuid) {
        List<String> actuallyTaken = new ArrayList<>();
        for (String id : ids) if (takeRecipe(id, uuid)) actuallyTaken.add(id);
        return actuallyTaken;
    }

    public static CompoundTag getPlayerData(Player player) {
        String uuid = player.getStringUUID();
        CompoundTag data = new CompoundTag();
        if (!knownRecipes.containsKey(uuid)) return data;
        if (knownRecipes.get(uuid).isEmpty()) return data;
        ListTag recipes = new ListTag();
        knownRecipes.get(uuid).stream().map(StringTag::valueOf).forEach(recipes::add);
        data.put("recipes", recipes);
        return data;
    }

}
