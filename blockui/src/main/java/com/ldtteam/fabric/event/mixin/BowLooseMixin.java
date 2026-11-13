package com.ldtteam.fabric.event.mixin;

import com.ldtteam.fabric.event.EventBridge;
import com.ldtteam.fabric.event.entity.player.ArrowLooseEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** ArrowLooseEvent: a player releasing a bow; a cancel shoots nothing. */
@Mixin(BowItem.class)
public abstract class BowLooseMixin
{
    @Inject(method = "releaseUsing", at = @At("HEAD"), cancellable = true)
    private void ldtteam$loose(final ItemStack stack, final Level level, final LivingEntity shooter, final int timeLeft, final CallbackInfoReturnable<Boolean> cir)
    {
        if (shooter instanceof final Player player && EventBridge.wants(ArrowLooseEvent.class)
              && EventBridge.post(new ArrowLooseEvent(player, stack, ((BowItem) (Object) this).getUseDuration(stack, shooter) - timeLeft)).isCanceled())
        {
            cir.setReturnValue(false);
        }
    }
}