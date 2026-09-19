package com.gibbdev.recipebookplus.interfaces.accessors;

import net.minecraft.world.item.ItemStack;

public interface IGhostRecipeAccessor {
    ItemStack getGhostItem(double mouseX, double mouseY);
}
