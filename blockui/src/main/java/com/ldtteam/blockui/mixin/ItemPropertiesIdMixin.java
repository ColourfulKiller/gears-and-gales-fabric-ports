package com.ldtteam.blockui.mixin;

import com.ldtteam.fabric.registry.PendingIds;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Item counterpart of {@link BlockPropertiesIdMixin}.
 */
@Mixin(Item.Properties.class)
public abstract class ItemPropertiesIdMixin
{
    @Shadow
    private @Nullable ResourceKey<Item> id;

    private void ldtfabric$fillId()
    {
        if (id == null)
        {
            // built outside a DeferredRegister and before its id is known (e.g. new Block(...).registerBlock(registry)):
            // a placeholder now, corrected by RegistryFixupMixin when it is registered
            id = PendingIds.item() != null ? PendingIds.item() : PendingIds.placeholderItem();
        }
    }

    @Inject(method = "effectiveDescriptionId", at = @At("HEAD"))
    private void ldtfabric$idForDescription(final CallbackInfoReturnable<String> cir)
    {
        ldtfabric$fillId();
    }

    @Inject(method = "effectiveModel", at = @At("HEAD"))
    private void ldtfabric$idForModel(final CallbackInfoReturnable<Identifier> cir)
    {
        ldtfabric$fillId();
    }
}
