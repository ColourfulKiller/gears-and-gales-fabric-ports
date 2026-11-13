package com.ldtteam.fabric.event.lifecycle;

import com.ldtteam.fabric.event.bus.Event;

import java.util.concurrent.CompletableFuture;

/** NeoForge's common setup: on Fabric the work simply runs in the mod initializer, after registration. */
public class FMLCommonSetupEvent extends Event
{
    public CompletableFuture<Void> enqueueWork(final Runnable work)
    {
        work.run();
        return CompletableFuture.completedFuture(null);
    }
}