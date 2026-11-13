package com.ldtteam.fabric.event.mixin;

import com.ldtteam.fabric.event.EventBridge;
import com.ldtteam.fabric.event.level.ExplosionEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/** ExplosionEvent.Start (cancel = no explosion) and ExplosionEvent.Detonate (handlers may drop affected blocks). */
@Mixin(ServerExplosion.class)
public abstract class ExplosionEventsMixin
{
    @Shadow
    @Final
    private ServerLevel level;

    @Shadow
    @Final
    private Vec3 center;

    @Shadow
    @Final
    private float radius;

    @Inject(method = "explode", at = @At("HEAD"), cancellable = true)
    private void ldtteam$start(final CallbackInfoReturnable<Integer> cir)
    {
        if (EventBridge.wants(ExplosionEvent.Start.class)
              && EventBridge.post(new ExplosionEvent.Start(level, (ServerExplosion) (Object) this)).isCanceled())
        {
            cir.setReturnValue(0);
        }
    }

    @ModifyVariable(method = "explode", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private List<BlockPos> ldtteam$detonate(final List<BlockPos> blocks)
    {
        if (!EventBridge.wants(ExplosionEvent.Detonate.class))
        {
            return blocks;
        }
        final double r = radius * 2.0;
        final List<Entity> entities = level.getEntities((Entity) null, new AABB(center, center).inflate(r));
        final List<BlockPos> mutable = new ArrayList<>(blocks);
        EventBridge.post(new ExplosionEvent.Detonate(level, (ServerExplosion) (Object) this, mutable, entities));
        return mutable;
    }
}