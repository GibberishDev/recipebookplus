package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.interfaces.IAbstractContainerScreen;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.awt.*;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin implements IAbstractContainerScreen, GuiEventListener {

    @Unique
    Point mousePos = new Point();

    @Shadow
    protected Slot hoveredSlot;

    @Override
    public Slot rbp$getSlotUnderCursor() {
        return this.hoveredSlot;
    }

    @Unique
    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        mousePos.setLocation(mouseX, mouseY);
    }

    @Unique
    @Override
    public Point rbp$getMousePos() {
        return mousePos;
    }
}
