package com.gibbdev.recipebookplus.compat;

import com.gibbdev.recipebookplus.interfaces.IFarmersDelightCompat;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.crafting.Recipe;
import vectorwing.farmersdelight.client.recipebook.CookingPotRecipeBookTab;
import vectorwing.farmersdelight.client.recipebook.RecipeCategories;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

public class FarmersDelightNeoforge implements IFarmersDelightCompat {

    @Override
    public RecipeBookCategories getRecipeCategory(Recipe<?> cookingRecipe) {
        if (cookingRecipe instanceof CookingPotRecipe) {
            CookingPotRecipeBookTab tab = ((CookingPotRecipe) cookingRecipe).getRecipeBookTab();
            switch (tab) {
                case CookingPotRecipeBookTab.MEALS -> {return RecipeCategories.COOKING_MEALS;}
                case CookingPotRecipeBookTab.DRINKS -> {return RecipeCategories.COOKING_DRINKS;}
                case CookingPotRecipeBookTab.MISC -> {return RecipeCategories.COOKING_MISC;}
                case null -> {return null;}
            }
        }
        return null;
    }
}
