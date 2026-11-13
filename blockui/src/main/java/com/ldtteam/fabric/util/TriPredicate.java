package com.ldtteam.fabric.util;

/** NeoForge's TriPredicate. */
@FunctionalInterface
public interface TriPredicate<T, U, V>
{
    boolean test(T t, U u, V v);

    default TriPredicate<T, U, V> and(final TriPredicate<? super T, ? super U, ? super V> other)
    {
        return (t, u, v) -> test(t, u, v) && other.test(t, u, v);
    }

    default TriPredicate<T, U, V> negate()
    {
        return (t, u, v) -> !test(t, u, v);
    }

    default TriPredicate<T, U, V> or(final TriPredicate<? super T, ? super U, ? super V> other)
    {
        return (t, u, v) -> test(t, u, v) || other.test(t, u, v);
    }
}