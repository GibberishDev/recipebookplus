package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.interfaces.accessors.IRecipeBookAccessor;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.stats.RecipeBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(ClientRecipeBook.class)
public class ClientRecipeBookMixin extends RecipeBook implements IRecipeBookAccessor {

    @Shadow
    private List<RecipeCollection> allCollections;

    @Unique
    @Override
    public void recipebookplus$clear() {
        this.known.clear();
        this.highlight.clear();
        ImmutableList.Builder<RecipeCollection> builder = ImmutableList.builder();
        this.allCollections = builder.build();
    }
}
