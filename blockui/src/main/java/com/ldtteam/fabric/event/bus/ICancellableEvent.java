package com.ldtteam.fabric.event.bus;

/** Marks events whose cancellation the bridge honours. */
public interface ICancellableEvent
{
    void setCanceled(boolean canceled);

    boolean isCanceled();
}
