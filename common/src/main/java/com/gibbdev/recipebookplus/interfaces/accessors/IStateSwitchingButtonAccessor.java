package com.gibbdev.recipebookplus.interfaces.accessors;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public interface IStateSwitchingButtonAccessor {
    void rbp$setClickSound(Holder.Reference<SoundEvent> soundEvent);
    void rbp$setClickSound(SoundEvent soundEvent);
    void rbp$setClickSound(Holder.Reference<SoundEvent> soundEventPressed,Holder.Reference<SoundEvent> soundEventReleased);
    void rbp$setClickSound(SoundEvent soundEventPressed,SoundEvent soundEventReleased);
}
