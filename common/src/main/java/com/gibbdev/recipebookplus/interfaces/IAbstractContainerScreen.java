package com.gibbdev.recipebookplus.interfaces;

import net.minecraft.world.inventory.Slot;

import java.awt.*;

public interface IAbstractContainerScreen {
    Slot rbp$getSlotUnderCursor();
    Point rbp$getMousePos();
}
