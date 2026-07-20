package com.gibbdev.recipebookplus.interfaces;

import net.minecraft.client.gui.components.EditBox;

public interface IRecipeBookComponentMixin {
    void rbp$resetSearch();
    EditBox rbp$getSearchBox();
}
