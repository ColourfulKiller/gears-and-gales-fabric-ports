package com.ldtteam.fabric.event.mixin;

import com.ldtteam.fabric.event.EventBridge;
import com.ldtteam.fabric.event.entity.item.ItemTossEvent;
import com.ldtteam.fabric.event.tick.PlayerTickEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** ItemTossEvent: after a player drops an item; a cancel removes the dropped entity (handlers give the item back). */
@Mixin(ServerPlayer.class)
public abstract class PlayerItemEventsMixin
{
    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("RETURN"), cancellable = true)
    private void ldtteam$toss(final ItemStack stack, final boolean randomly, final boolean retainOwnership, final CallbackInfoReturnable<ItemEntity> cir)
    {
        final ItemEntity entity = cir.getReturnValue();
        if (entity != null && EventBridge.wants(ItemTossEvent.class)
              && EventBridge.post(new ItemTossEvent(entity, (ServerPlayer) (Object) this)).isCanceled())
        {
            entity.discard();
            cir.setReturnValue(null);
        }
    }
}