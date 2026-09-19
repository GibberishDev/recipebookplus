package com.gibbdev.recipebookplus.interfaces;

import net.minecraft.world.item.ItemStack;

public interface IRecipeBookComponent {
    void rbp$search(String searchTerm);
    ItemStack rbp$getGhostItemStack(double mouseX, double mouseY);
}
