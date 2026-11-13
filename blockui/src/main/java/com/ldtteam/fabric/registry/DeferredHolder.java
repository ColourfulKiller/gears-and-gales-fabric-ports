package com.ldtteam.fabric.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Fabric replacement for NeoForge's DeferredHolder: a handle to a registry entry that exists once its
 * {@link DeferredRegister} has run {@link DeferredRegister#register()}.
 *
 * @param <R> registry type
 * @param <T> entry type
 */
public class DeferredHolder<R, T extends R> implements Supplier<T>
{
    protected final ResourceKey<R> key;
    private @Nullable Holder<R> holder;

    protected DeferredHolder(final ResourceKey<R> key)
    {
        this.key = key;
    }

    public static <R, T extends R> DeferredHolder<R, T> create(final ResourceKey<? extends Registry<R>> registryKey, final Identifier id)
    {
        return new DeferredHolder<>(ResourceKey.create(registryKey, id));
    }

    void bind(final Holder<R> holder)
    {
        this.holder = holder;
    }

    @SuppressWarnings("unchecked")
    private Holder<R> holder()
    {
        if (holder == null)
        {
            final Registry<R> registry = (Registry<R>) BuiltInRegistries.REGISTRY.getValue(key.registry());
            holder = Objects.requireNonNull(registry, "Unknown registry " + key.registry()).get(key)
                .orElseThrow(() -> new IllegalStateException("Registry entry not present: " + key));
        }
        return holder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get()
    {
        return (T) holder().value();
    }

    public T value()
    {
        return get();
    }

    /** The vanilla holder for this entry (only after registration). */
    public Holder<R> getDelegate()
    {
        return holder();
    }

    public Identifier getId()
    {
        return key.identifier();
    }

    public ResourceKey<R> getKey()
    {
        return key;
    }

    public Optional<ResourceKey<R>> unwrapKey()
    {
        return Optional.of(key);
    }

    public boolean isBound()
    {
        return holder != null;
    }

    @Override
    public String toString()
    {
        return "DeferredHolder{" + key + "}";
    }
}
