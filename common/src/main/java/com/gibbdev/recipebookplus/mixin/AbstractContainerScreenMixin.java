package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.interfaces.IAbstractContainerScreenMixin;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin implements IAbstractContainerScreenMixin {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    public Slot rbp$getHoveredSlot() {
        return this.hoveredSlot;
    }

}
