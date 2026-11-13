package com.ldtteam.fabric.event.client;

import com.ldtteam.fabric.event.bus.Event;

/** NeoForge's ClientPlayerNetworkEvent, fired by ClientEventBridge on connect / disconnect. */
public abstract class ClientPlayerNetworkEvent extends Event
{
    public static class LoggingIn extends ClientPlayerNetworkEvent
    {
    }

    public static class LoggingOut extends ClientPlayerNetworkEvent
    {
    }
}