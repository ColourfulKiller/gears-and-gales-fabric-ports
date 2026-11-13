package com.ldtteam.fabric.event.bus;

/** Base of the NeoForge-shaped events (com.ldtteam.fabric.event). */
public abstract class Event
{
    private boolean canceled;

    /** Only meaningful for {@link ICancellableEvent}s. */
    public void setCanceled(final boolean canceled)
    {
        this.canceled = canceled;
    }

    public boolean isCanceled()
    {
        return canceled;
    }
}
