package com.gibbdev.recipebookplus.interfaces;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.item.ItemStack;

public interface IAbstractRecipeBookScreenMixin {
    RecipeBookComponent<?> rbp$getRecipeBookComponent();
    ItemStack rbp$getSlotUnderCursor();
}
