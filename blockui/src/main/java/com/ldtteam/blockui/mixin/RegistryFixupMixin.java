package com.ldtteam.blockui.mixin;

import com.ldtteam.fabric.registry.PendingIds;
import net.minecraft.util.Util;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Set;

/**
 * Corrects blocks / items that were built with a placeholder id (see PendingIds) once they are registered under their
 * real id: description id, loot table, item name and item model. Also gives block items of the LDT mods their block's
 * description id, which BlockItem#getDescriptionId did in 1.21.1 (1.21.2+ only does it with useBlockDescriptionPrefix).
 */
@Mixin(MappedRegistry.class)
public abstract class RegistryFixupMixin
{
    private static final Set<String> LDT_NAMESPACES = Set.of("blockui", "domum_ornamentum", "structurize", "multipiston", "minecolonies");

    @Inject(method = "register", at = @At("HEAD"))
    private void ldtfabric$fixIds(final ResourceKey<?> key, final Object value, final RegistrationInfo info, final CallbackInfoReturnable<?> cir)
    {
        final Identifier id = key.identifier();
        if (value instanceof final Block block && key.isFor(Registries.BLOCK))
        {
            final BlockBehaviourIdAccessor acc = (BlockBehaviourIdAccessor) block;
            final String placeholderPrefix = "block." + PendingIds.PLACEHOLDER_NAMESPACE + ".";
            if (acc.ldtfabric$getDescriptionId().startsWith(placeholderPrefix))
            {
                acc.ldtfabric$setDescriptionId(Util.makeDescriptionId("block", id));
                acc.ldtfabric$getDrops().ifPresent(drops -> {
                    if (drops.identifier().getNamespace().equals(PendingIds.PLACEHOLDER_NAMESPACE))
                    {
                        acc.ldtfabric$setDrops(Optional.of(ResourceKey.create(Registries.LOOT_TABLE, id.withPrefix("blocks/"))));
                    }
                });
            }
        }
        else if (value instanceof final Item item && key.isFor(Registries.ITEM))
        {
            final ItemIdAccessor acc = (ItemIdAccessor) item;
            String description = acc.ldtfabric$getDescriptionId();
            boolean changed = false;
            for (final String kind : new String[] {"item", "block"})
            {
                final String placeholderPrefix = kind + "." + PendingIds.PLACEHOLDER_NAMESPACE + ".";
                if (description.startsWith(placeholderPrefix))
                {
                    description = Util.makeDescriptionId(kind, id);
                    changed = true;
                }
            }
            if (item instanceof final BlockItem blockItem && LDT_NAMESPACES.contains(id.getNamespace())
                  && description.equals(Util.makeDescriptionId("item", id)))
            {
                description = blockItem.getBlock().getDescriptionId();
                changed = true;
            }
            final DataComponentMap components = acc.ldtfabric$getComponents();
            final Identifier model = components.get(DataComponents.ITEM_MODEL);
            final boolean placeholderModel = model != null && model.getNamespace().equals(PendingIds.PLACEHOLDER_NAMESPACE);
            if (changed || placeholderModel)
            {
                acc.ldtfabric$setDescriptionId(description);
                final DataComponentMap.Builder builder = DataComponentMap.builder().addAll(components);
                if (changed)
                {
                    builder.set(DataComponents.ITEM_NAME, Component.translatable(description));
                }
                if (placeholderModel)
                {
                    builder.set(DataComponents.ITEM_MODEL, id);
                }
                acc.ldtfabric$setComponents(builder.build());
            }
        }
    }
}