package com.ldtteam.structurize.mixin.client;

import com.ldtteam.structurize.event.ClientEventSubscriber;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces NeoForge's InputEvent.MouseScrollingEvent: Shift + scroll on a Structurize tool adjusts the tool instead of
 * scrolling the hotbar.
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin
{
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void structurize$onScroll(final long window, final double xOffset, final double yOffset, final CallbackInfo ci)
    {
        if (window == minecraft.getWindow().handle() && minecraft.getOverlay() == null && ClientEventSubscriber.onMouseWheel(xOffset, yOffset))
        {
            ci.cancel();
        }
    }
}