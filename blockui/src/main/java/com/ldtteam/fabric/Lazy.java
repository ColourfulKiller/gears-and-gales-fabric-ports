package com.ldtteam.fabric;

import java.util.function.Supplier;

/**
 * Fabric replacement for NeoForge's net.neoforged.neoforge.common.util.Lazy: a supplier that computes its value once.
 */
public final class Lazy<T> implements Supplier<T>
{
    private Supplier<T> factory;
    private T value;
    private boolean done;

    private Lazy(final Supplier<T> factory)
    {
        this.factory = factory;
    }

    public static <T> Lazy<T> of(final Supplier<T> factory)
    {
        return new Lazy<>(factory);
    }

    @Override
    public synchronized T get()
    {
        if (!done)
        {
            value = factory.get();
            factory = null;
            done = true;
        }
        return value;
    }
}
