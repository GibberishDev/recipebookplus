package com.gibbdev.recipebookplus.interfaces;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Unique;

public interface IAbstractRecipeBookScreenMixin {
    RecipeBookComponent<?> rbp$getRecipeBookComponent();
    ItemStack rbp$getSlotUnderCursor();
    void rbp$openRecipeBook();
}
