package com.gibbdev.recipebookplus.compat;

import com.gibbdev.recipebookplus.interfaces.IFarmersDelightCompat;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.crafting.Recipe;
import vectorwing.farmersdelight.client.recipebook.CookingPotRecipeBookTab;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.refabricated.client.FDRecipeCategories;

public class FarmersDelightRefabricated implements IFarmersDelightCompat {
    @Override
    public RecipeBookCategories getRecipeCategory(Recipe<?> cookingRecipe) {
        if (cookingRecipe instanceof CookingPotRecipe) {
            CookingPotRecipeBookTab tab = ((CookingPotRecipe) cookingRecipe).getRecipeBookTab();
            switch (tab) {
                case CookingPotRecipeBookTab.MEALS -> {return FDRecipeCategories.COOKING_MEALS;}
                case CookingPotRecipeBookTab.DRINKS -> {return FDRecipeCategories.COOKING_DRINKS;}
                case CookingPotRecipeBookTab.MISC -> {return FDRecipeCategories.COOKING_MISC;}
                case null -> {return null;}
            }
        }
        return null;
    }
}
