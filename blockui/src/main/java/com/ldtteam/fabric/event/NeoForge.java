package com.ldtteam.fabric.event;

import com.ldtteam.fabric.event.bus.EventBus;

/** NeoForge's game event bus. Events are fired into it by {@link EventBridge}. */
public final class NeoForge
{
    public static final EventBus EVENT_BUS = new EventBus();

    private NeoForge()
    {
    }
}