package com.ldtteam.blockui.mixin;

import com.ldtteam.fabric.registry.PendingIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * 1.21.2+ needs Properties.setId before a Block is built; NeoForge 1.21.1 code never calls it. When a block is created
 * by com.ldtteam.fabric.registry.DeferredRegister, fill in the id it is being registered under.
 */
@Mixin(BlockBehaviour.Properties.class)
public abstract class BlockPropertiesIdMixin
{
    @Shadow
    private @Nullable ResourceKey<Block> id;

    private void ldtfabric$fillId()
    {
        if (id == null)
        {
            // built outside a DeferredRegister and before its id is known (e.g. new Block(...).registerBlock(registry)):
            // a placeholder now, corrected by RegistryFixupMixin when it is registered
            id = PendingIds.block() != null ? PendingIds.block() : PendingIds.placeholderBlock();
        }
    }

    @Inject(method = "effectiveDrops", at = @At("HEAD"))
    private void ldtfabric$idForDrops(final CallbackInfoReturnable<Optional<?>> cir)
    {
        ldtfabric$fillId();
    }

    @Inject(method = "effectiveDescriptionId", at = @At("HEAD"))
    private void ldtfabric$idForDescription(final CallbackInfoReturnable<String> cir)
    {
        ldtfabric$fillId();
    }
}
