package com.ldtteam.fabric.event.mixin;

import com.ldtteam.fabric.event.EventBridge;
import com.ldtteam.fabric.event.VanillaGameEvent;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** EntityJoinLevelEvent for fresh entities (cancel refuses the spawn) and VanillaGameEvent. */
@Mixin(ServerLevel.class)
public abstract class ServerLevelEventsMixin
{
    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void ldtteam$joinEvent(final Entity entity, final CallbackInfoReturnable<Boolean> cir)
    {
        if (!EventBridge.onEntityAdded(entity, (ServerLevel) (Object) this))
        {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "gameEvent", at = @At("HEAD"), cancellable = true)
    private void ldtteam$gameEvent(final Holder<GameEvent> event, final Vec3 pos, final GameEvent.Context context, final CallbackInfo ci)
    {
        if (EventBridge.wants(VanillaGameEvent.class)
              && EventBridge.post(new VanillaGameEvent((ServerLevel) (Object) this, event, pos, context)).isCanceled())
        {
            ci.cancel();
        }
    }
}