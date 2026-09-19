package com.gibbdev.recipebookplus;

import net.minecraft.resources.ResourceLocation;

public class RecipeBookPlus {
    public static boolean groupingState = true;


    public static ResourceLocation rl(String id) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, id);
    }
}