package com.gibbdev.recipebookplus.compat;

import com.gibbdev.recipebookplus.interfaces.IFarmersDelightCompat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

public class FarmersDelight {

    public static IFarmersDelightCompat INSTANCE;

    public static ItemStack getRecipeContainer(RecipeHolder<?> holder) {
        if (holder.value().getType()== ModRecipeTypes.COOKING.get()) {
            return ((CookingPotRecipe) holder.value()).getOutputContainer();
        }
        return ItemStack.EMPTY;
    }

}
