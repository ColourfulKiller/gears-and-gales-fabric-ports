package com.ldtteam.fabric.event.client;

import com.ldtteam.fabric.event.bus.Event;

/** NeoForge's ClientTickEvent, fired by ClientEventBridge. */
public abstract class ClientTickEvent extends Event
{
    public static class Pre extends ClientTickEvent
    {
    }

    public static class Post extends ClientTickEvent
    {
    }
}