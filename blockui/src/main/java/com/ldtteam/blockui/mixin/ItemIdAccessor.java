package com.ldtteam.blockui.mixin;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Lets RegistryFixupMixin correct id-derived item fields. */
@Mixin(Item.class)
public interface ItemIdAccessor
{
    @Accessor("descriptionId")
    String ldtfabric$getDescriptionId();

    @Mutable
    @Accessor("descriptionId")
    void ldtfabric$setDescriptionId(String descriptionId);

    @Accessor("components")
    DataComponentMap ldtfabric$getComponents();

    @Mutable
    @Accessor("components")
    void ldtfabric$setComponents(DataComponentMap components);
}