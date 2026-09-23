package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.interfaces.IAbstractContainerScreen;
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
    Point recipebookplus$mousePos = new Point();

    @Shadow
    protected Slot hoveredSlot;

    @Override
    public Slot recipebookplus$getSlotUnderCursor() {
        return this.hoveredSlot;
    }

    @Unique
    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        recipebookplus$mousePos.setLocation(mouseX, mouseY);
    }

    @Unique
    @Override
    public Point recipebookplus$getMousePos() {
        return recipebookplus$mousePos;
    }
}
