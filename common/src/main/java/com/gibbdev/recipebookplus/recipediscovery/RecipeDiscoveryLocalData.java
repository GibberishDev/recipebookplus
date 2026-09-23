package com.gibbdev.recipebookplus.recipediscovery;

import com.gibbdev.recipebookplus.Constants;
import com.gibbdev.recipebookplus.interfaces.IRecipeDiscovery;
import com.gibbdev.recipebookplus.networking.ModStatus;
import com.gibbdev.recipebookplus.platform.Services;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class RecipeDiscoveryLocalData implements IRecipeDiscovery {

    static final Set<String> knownRecipes = new HashSet<>();
    private static boolean isDirty = false;

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    private Path file;

    public void save() {
        if (!isDirty) return;
        isDirty = false;
        try {
            Files.createDirectories(file.getParent());
            LocalData data = new LocalData();
            data.knownRecipes.addAll(knownRecipes);
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save recipe discovery data to " + file, e);
        }
    }

    public void load() {
        if (!Files.exists(file)) return;
        try (Reader reader = Files.newBufferedReader(file)) {
            LocalData data = GSON.fromJson(reader,LocalData.class);
            if (data != null) {
                knownRecipes.clear();
                knownRecipes.addAll(data.knownRecipes);
            }
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException("Failed to load recipe discovery data from " + file, e);
        }
        Constants.LOG.info("Loaded {} recipes from local storage as server does not have RB+ mod installed", knownRecipes.size());
    }

    public RecipeDiscoveryLocalData(String serverID) {
        if (!Minecraft.getInstance().isSingleplayer() && !ModStatus.getInstalled()) {
            Path dir = Services.PLATFORM.getInstanceDirectory()
                    .resolve("data").resolve(Constants.MOD_ID);
            this.file = dir.resolve(serverID + ".json");
            load();
        }
    }

    @Override
    public List<RecipeHolder<?>> getKnownHolders(UUID uuid) {
        List<RecipeHolder<?>> list = new ArrayList<>();
        if (Minecraft.getInstance().level == null) return list;
        for (String id : knownRecipes) {
            Optional<RecipeHolder<?>> holder = Minecraft.getInstance().level.getRecipeManager().byKey(ResourceLocation.parse(id));
            if (holder.isEmpty()) continue;
            list.add(holder.get());
        }
        return list;
    }

    @Override
    public boolean hasAll(List<String> ids, UUID uuid) {
        return knownRecipes.containsAll(ids);
    }

    @Override
    public boolean isKnown(String id, UUID uuid) {
        return knownRecipes.contains(id);
    }

    @Override
    public boolean giveRecipe(String id, UUID uuid) {
        if (!isKnown(id, uuid)) {
            knownRecipes.add(id);
            isDirty = true;
            //HACK: change to dispatcher
            save();
            return true;
        }
        return false;
    }

    @Override
    public List<String> giveRecipe(List<String> ids, UUID uuid) {
        List<String> actuallyAwarded = new ArrayList<>();
        for (String id : ids) if (giveRecipe(id, uuid)) actuallyAwarded.add(id);
        return actuallyAwarded;
    }

    @Override
    public boolean takeRecipe(String id, UUID uuid) {
        if (isKnown(id, uuid)) {
            knownRecipes.remove(id);
            isDirty = true;
            //HACK: change to dispatcher
            save();
            return true;
        }
        return false;
    }

    @Override
    public List<String> takeRecipe(List<String> ids, UUID uuid) {
        List<String> actuallyTaken = new ArrayList<>();
        for (String id : ids) if (takeRecipe(id, uuid)) actuallyTaken.add(id);
        return actuallyTaken;
    }

    private static class LocalData {
        private final Set<String> knownRecipes = new HashSet<>();
    }
}
