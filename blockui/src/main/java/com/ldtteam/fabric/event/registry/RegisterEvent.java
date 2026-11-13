package com.ldtteam.fabric.event.registry;

import com.ldtteam.fabric.event.bus.Event;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

/**
 * NeoForge's RegisterEvent, posted once per registry (in vanilla order) on a mod's own bus during its initializer.
 * Registration goes straight into the built-in registry.
 */
public class RegisterEvent extends Event
{
    private final ResourceKey<? extends Registry<?>> registryKey;

    public RegisterEvent(final ResourceKey<? extends Registry<?>> registryKey)
    {
        this.registryKey = registryKey;
    }

    public ResourceKey<? extends Registry<?>> getRegistryKey()
    {
        return registryKey;
    }

    @SuppressWarnings("unchecked")
    public <T> Registry<T> getRegistry(final ResourceKey<? extends Registry<T>> key)
    {
        return key.equals(registryKey) ? (Registry<T>) BuiltInRegistries.REGISTRY.getValue(key.identifier()) : null;
    }

    public <T> void register(final ResourceKey<? extends Registry<T>> key, final Identifier id, final Supplier<T> value)
    {
        final Registry<T> registry = getRegistry(key);
        if (registry != null)
        {
            Registry.register(registry, id, value.get());
        }
    }
}