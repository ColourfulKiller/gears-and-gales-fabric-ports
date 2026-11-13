package com.ldtteam.fabric.registry;

import com.ldtteam.fabric.event.bus.IEventBus;
import com.ldtteam.fabric.event.registry.RegisterEvent;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Runs a mod's registration the way NeoForge did: registry by registry, firing {@link RegisterEvent} on the mod's bus
 * and registering the mod's {@link DeferredRegister}s for that registry in the same step. Order: data components,
 * attributes, sounds, blocks, fluids, entity types (spawn eggs need them), items, block entity types, the rest of the vanilla registries,
 * then the mod's own (custom) registries in creation order.
 */
public final class ModRegistration
{
    private static final List<ResourceKey<? extends Registry<?>>> FIRST = List.of(
        Registries.DATA_COMPONENT_TYPE, Registries.ATTRIBUTE, Registries.SOUND_EVENT, Registries.BLOCK, Registries.FLUID,
        Registries.ENTITY_TYPE, Registries.ITEM, Registries.BLOCK_ENTITY_TYPE);

    private ModRegistration()
    {
    }

    public static void run(final IEventBus modBus, final List<DeferredRegister<?>> registers)
    {
        final Set<ResourceKey<? extends Registry<?>>> order = new LinkedHashSet<>(FIRST);
        for (final Registry<?> registry : BuiltInRegistries.REGISTRY)
        {
            order.add(registry.key());
        }
        final List<DeferredRegister<?>> pending = new ArrayList<>(registers);
        for (final ResourceKey<? extends Registry<?>> key : order)
        {
            modBus.post(new RegisterEvent(key));
            pending.removeIf(register -> {
                if (register.getRegistryKey().equals(key))
                {
                    register.register();
                    return true;
                }
                return false;
            });
        }
        // registries that only exist by name (none expected): register what is left
        pending.forEach(DeferredRegister::register);
    }
}