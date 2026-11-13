package com.ldtteam.fabric.event.mixin;

import com.ldtteam.fabric.event.EventBridge;
import com.ldtteam.fabric.event.entity.living.LivingConversionEvent;
import com.ldtteam.fabric.event.entity.living.MobSpawnEvent;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** MobSpawnEvent.PositionCheck (spawn-rule check result) and LivingConversionEvent.Pre (cancel stops the conversion). */
@Mixin(Mob.class)
public abstract class MobEventsMixin
{
    @Inject(method = "checkSpawnRules", at = @At("RETURN"), cancellable = true)
    private void ldtteam$spawnCheck(final LevelAccessor level, final EntitySpawnReason reason, final CallbackInfoReturnable<Boolean> cir)
    {
        if (!(level instanceof final ServerLevelAccessor serverLevel) || !EventBridge.wants(MobSpawnEvent.PositionCheck.class))
        {
            return;
        }
        final Mob self = (Mob) (Object) this;
        final MobSpawnEvent.PositionCheck event = EventBridge.post(new MobSpawnEvent.PositionCheck(self, serverLevel, self.getX(), self.getY(), self.getZ(), reason));
        if (event.getResult() == MobSpawnEvent.PositionCheck.Result.FAIL)
        {
            cir.setReturnValue(false);
        }
        else if (event.getResult() == MobSpawnEvent.PositionCheck.Result.SUCCEED)
        {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;",
        at = @At("HEAD"), cancellable = true)
    private <T extends Mob> void ldtteam$convert(final EntityType<T> type, final ConversionParams params, final EntitySpawnReason reason,
        final ConversionParams.AfterConversion<T> after, final CallbackInfoReturnable<T> cir)
    {
        if (EventBridge.wants(LivingConversionEvent.Pre.class)
              && EventBridge.post(new LivingConversionEvent.Pre((Mob) (Object) this, type)).isCanceled())
        {
            cir.setReturnValue(null);
        }
    }
}