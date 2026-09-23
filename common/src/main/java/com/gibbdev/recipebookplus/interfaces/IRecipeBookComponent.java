package com.gibbdev.recipebookplus.interfaces;

import net.minecraft.world.item.ItemStack;

public interface IRecipeBookComponent {
    void recipebookplus$search(String searchTerm);
    ItemStack recipebookplus$getGhostItemStack(double mouseX, double mouseY);
    ItemStack recipebookplus$getRecipeButtonDisplayItemStack(double mouseX, double mouseY);
}
