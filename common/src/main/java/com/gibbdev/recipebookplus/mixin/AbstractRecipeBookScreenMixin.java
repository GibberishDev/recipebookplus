package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.interfaces.IAbstractContainerScreenMixin;
import com.gibbdev.recipebookplus.interfaces.IAbstractRecipeBookScreenMixin;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractRecipeBookScreen.class)
public abstract class AbstractRecipeBookScreenMixin implements IAbstractRecipeBookScreenMixin {

    @Shadow
    @Final
    private RecipeBookComponent<?> recipeBookComponent;



    @Unique @Override
    public RecipeBookComponent<?> rbp$getRecipeBookComponent() {
        return recipeBookComponent;
    }

    @Unique @Override
    public ItemStack rbp$getSlotUnderCursor() {
        Slot slot = ((IAbstractContainerScreenMixin) (Object) this).rbp$getHoveredSlot();
        if (slot.hasItem()) return slot.getItem();
        return null;
    }

}
