package com.gibbdev.recipebookplus.interfaces.accessors;

import org.spongepowered.asm.mixin.Unique;

public interface IRecipeBookAccessor {
    @Unique
    void recipebookplus$clear();
}
