package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Keybinds;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "keyPressed", at=@At("TAIL"))
    public void keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        Keybinds.KeybindEvent((Screen) (Object) this,event);
    }

}
