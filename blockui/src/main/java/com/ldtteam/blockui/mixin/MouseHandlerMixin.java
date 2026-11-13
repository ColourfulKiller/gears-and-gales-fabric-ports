package com.ldtteam.blockui.mixin;

import com.ldtteam.blockui.hooks.HookManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces NeoForge's InputEvent.MouseScrollingEvent: while no screen is open, a world-space BlockUI window under the
 * crosshair (hooks) can take the scroll wheel, and then the hotbar doesn't scroll.
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin
{
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void blockui$onScroll(final long window, final double xOffset, final double yOffset, final CallbackInfo ci)
    {
        if (window == minecraft.getWindow().handle() && minecraft.screen == null && minecraft.getOverlay() == null &&
            HookManager.onScroll(xOffset, yOffset))
        {
            ci.cancel();
        }
    }
}
