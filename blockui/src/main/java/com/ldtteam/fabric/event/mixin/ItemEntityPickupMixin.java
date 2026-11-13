package com.ldtteam.fabric.event.mixin;

import com.ldtteam.fabric.event.EventBridge;
import com.ldtteam.fabric.event.entity.player.ItemEntityPickupEvent;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** ItemEntityPickupEvent.Pre: canPickup FALSE keeps the item on the ground. */
@Mixin(ItemEntity.class)
public abstract class ItemEntityPickupMixin
{
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void ldtteam$pickup(final Player player, final CallbackInfo ci)
    {
        if (!player.level().isClientSide() && EventBridge.wants(ItemEntityPickupEvent.Pre.class)
              && EventBridge.post(new ItemEntityPickupEvent.Pre(player, (ItemEntity) (Object) this)).canPickup() == TriState.FALSE)
        {
            ci.cancel();
        }
    }
}