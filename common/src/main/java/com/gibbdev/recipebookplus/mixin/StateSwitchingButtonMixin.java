package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.interfaces.accessors.IStateSwitchingButtonAccessor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(StateSwitchingButton.class)
public abstract class StateSwitchingButtonMixin extends AbstractWidget implements IStateSwitchingButtonAccessor {
    @Unique
    private SoundEvent rbp$clickSound = SoundEvents.UI_BUTTON_CLICK.value();
    @Unique
    private SoundEvent rbp$clickSoundReleased = SoundEvents.UI_BUTTON_CLICK.value();
    @Shadow
    protected boolean isStateTriggered;


    public StateSwitchingButtonMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Unique
    @Override
    public void rbp$setClickSound(Holder.Reference<SoundEvent> soundEvent) {
        rbp$clickSound = soundEvent.value();
        rbp$clickSoundReleased = soundEvent.value();
    }
    @Unique
    @Override
    public void rbp$setClickSound(SoundEvent soundEvent) {
        rbp$clickSound = soundEvent;
        rbp$clickSoundReleased = soundEvent;
    }

    @Unique
    @Override
    public void rbp$setClickSound(Holder.Reference<SoundEvent> soundEventPressed, Holder.Reference<SoundEvent> soundEventReleased) {
        rbp$clickSound = soundEventPressed.value();
        rbp$clickSoundReleased = soundEventReleased.value();
    }
    @Unique
    @Override
    public void rbp$setClickSound(SoundEvent soundEventPressed, SoundEvent soundEventReleased) {
        rbp$clickSound = soundEventPressed;
        rbp$clickSoundReleased = soundEventReleased;
    }

    @Override
    public void playDownSound(@NotNull SoundManager handler) {
        if (Config.getUseCustomUI() && Config.getModEnabled()) handler.play(SimpleSoundInstance.forUI(rbp$clickSound, 1.0F));
        else handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
