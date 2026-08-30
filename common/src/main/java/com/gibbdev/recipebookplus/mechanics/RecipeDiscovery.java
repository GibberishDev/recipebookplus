package com.gibbdev.recipebookplus.mechanics;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

public class RecipeDiscovery extends SavedData {

    private final Set<ResourceLocation> discovered = new HashSet<>();

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        return null;
    }
}
