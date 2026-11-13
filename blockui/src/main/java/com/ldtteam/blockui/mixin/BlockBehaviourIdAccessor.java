package com.ldtteam.blockui.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

/** Lets RegistryFixupMixin correct id-derived block fields. */
@Mixin(BlockBehaviour.class)
public interface BlockBehaviourIdAccessor
{
    @Accessor("drops")
    Optional<ResourceKey<LootTable>> ldtfabric$getDrops();

    @Mutable
    @Accessor("drops")
    void ldtfabric$setDrops(Optional<ResourceKey<LootTable>> drops);

    @Accessor("descriptionId")
    String ldtfabric$getDescriptionId();

    @Mutable
    @Accessor("descriptionId")
    void ldtfabric$setDescriptionId(String descriptionId);
}