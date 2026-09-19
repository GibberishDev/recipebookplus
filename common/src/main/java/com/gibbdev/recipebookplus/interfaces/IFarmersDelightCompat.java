package com.gibbdev.recipebookplus.interfaces;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.crafting.Recipe;

public interface IFarmersDelightCompat {
    RecipeBookCategories getRecipeCategory(Recipe<?> cookingRecipe);
}
