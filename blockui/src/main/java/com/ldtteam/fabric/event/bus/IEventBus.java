package com.ldtteam.fabric.event.bus;

import java.util.function.Consumer;

/** NeoForge's IEventBus surface used by the LDT mods. */
public interface IEventBus
{
    /** A class (its static {@link SubscribeEvent} methods) or an instance (its instance ones). */
    void register(Object target);

    void unregister(Object target);

    /** NeoForge inferred the event type from the lambda; Fabric code passes it explicitly. */
    <T extends Event> void addListener(Class<T> eventType, Consumer<T> listener);

    <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Class<T> eventType, Consumer<T> listener);

    <T extends Event> T post(T event);
}