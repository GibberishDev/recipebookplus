package com.gibbdev.recipebookplus.interfaces.accessors;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

public interface IStateSwitchingButtonAccessor {
    void recipebookplus$setClickSound(Holder.Reference<SoundEvent> soundEvent);
    void recipebookplus$setClickSound(SoundEvent soundEvent);
    void recipebookplus$setClickSound(Holder.Reference<SoundEvent> soundEventPressed,Holder.Reference<SoundEvent> soundEventReleased);
    void recipebookplus$setClickSound(SoundEvent soundEventPressed,SoundEvent soundEventReleased);
}
