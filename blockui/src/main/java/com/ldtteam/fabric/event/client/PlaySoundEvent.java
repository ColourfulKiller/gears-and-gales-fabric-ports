package com.ldtteam.fabric.event.client;

import com.ldtteam.fabric.event.bus.Event;
import net.minecraft.client.resources.sounds.SoundInstance;
import org.jetbrains.annotations.Nullable;

/** NeoForge's PlaySoundEvent: setSound(null) silences it (fired from com.ldtteam.fabric.event.mixin.client.SoundEngineMixin). */
public class PlaySoundEvent extends Event
{
    private @Nullable SoundInstance sound;

    public PlaySoundEvent(final SoundInstance sound)
    {
        this.sound = sound;
    }

    public @Nullable SoundInstance getSound()
    {
        return sound;
    }

    public void setSound(@Nullable final SoundInstance sound)
    {
        this.sound = sound;
    }
}