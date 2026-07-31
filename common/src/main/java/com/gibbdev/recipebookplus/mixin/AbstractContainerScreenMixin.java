package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.interfaces.IAbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin implements IAbstractContainerScreen {

    @Shadow
    protected Slot hoveredSlot;

    @Override
    public Slot rbp$getSlotUnderCursor() {
        return this.hoveredSlot;
    }
}
