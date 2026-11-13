package com.ldtteam.fabric.event.mixin;

import com.ldtteam.fabric.event.EventBridge;
import com.ldtteam.fabric.event.entity.EntityTravelToDimensionEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** EntityTravelToDimensionEvent: before a cross-dimension teleport; a cancel stops it. */
@Mixin(Entity.class)
public abstract class EntityTravelMixin
{
    @Inject(method = "teleport", at = @At("HEAD"), cancellable = true)
    private void ldtteam$travel(final TeleportTransition transition, final CallbackInfoReturnable<Entity> cir)
    {
        final Entity self = (Entity) (Object) this;
        if (transition.newLevel() != self.level() && EventBridge.wants(EntityTravelToDimensionEvent.class)
              && EventBridge.post(new EntityTravelToDimensionEvent(self, transition.newLevel().dimension())).isCanceled())
        {
            cir.setReturnValue(null);
        }
    }
}