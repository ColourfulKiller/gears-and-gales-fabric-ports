package com.ldtteam.fabric.event;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class VanillaGameEvent extends Event implements ICancellableEvent
{
    private final net.minecraft.world.level.Level level;
    private final net.minecraft.core.Holder<net.minecraft.world.level.gameevent.GameEvent> vanillaEvent;
    private final net.minecraft.world.phys.Vec3 eventPosition;
    private final net.minecraft.world.level.gameevent.GameEvent.Context context;

    public VanillaGameEvent(final net.minecraft.world.level.Level level, final net.minecraft.core.Holder<net.minecraft.world.level.gameevent.GameEvent> vanillaEvent, final net.minecraft.world.phys.Vec3 eventPosition, final net.minecraft.world.level.gameevent.GameEvent.Context context)
    {
        this.level = level;
        this.vanillaEvent = vanillaEvent;
        this.eventPosition = eventPosition;
        this.context = context;
    }

    public net.minecraft.world.level.Level getLevel()
    {
        return level;
    }

    public net.minecraft.core.Holder<net.minecraft.world.level.gameevent.GameEvent> getVanillaEvent()
    {
        return vanillaEvent;
    }

    public net.minecraft.world.phys.Vec3 getEventPosition()
    {
        return eventPosition;
    }

    public net.minecraft.world.level.gameevent.GameEvent.Context getContext()
    {
        return context;
    }
}
