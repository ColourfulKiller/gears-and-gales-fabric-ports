package com.ldtteam.fabric.event.mixin.client;

import com.ldtteam.fabric.event.EventBridge;
import com.ldtteam.fabric.event.client.PlaySoundEvent;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** PlaySoundEvent: a listener clearing the sound stops it from playing. */
@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin
{
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void ldtteam$playSound(final SoundInstance sound, final CallbackInfoReturnable<SoundEngine.PlayResult> cir)
    {
        if (EventBridge.wants(PlaySoundEvent.class) && EventBridge.post(new PlaySoundEvent(sound)).getSound() == null)
        {
            cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
        }
    }
}