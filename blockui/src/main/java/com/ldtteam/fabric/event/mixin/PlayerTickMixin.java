package com.ldtteam.fabric.event.mixin;

import com.ldtteam.fabric.event.EventBridge;
import com.ldtteam.fabric.event.tick.PlayerTickEvent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** PlayerTickEvent.Pre / Post (both sides, like NeoForge). */
@Mixin(Player.class)
public abstract class PlayerTickMixin
{
    @Inject(method = "tick", at = @At("HEAD"))
    private void ldtteam$tickPre(final CallbackInfo ci)
    {
        if (EventBridge.wants(PlayerTickEvent.Pre.class))
        {
            EventBridge.post(new PlayerTickEvent.Pre((Player) (Object) this));
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void ldtteam$tickPost(final CallbackInfo ci)
    {
        if (EventBridge.wants(PlayerTickEvent.Post.class))
        {
            EventBridge.post(new PlayerTickEvent.Post((Player) (Object) this));
        }
    }
}