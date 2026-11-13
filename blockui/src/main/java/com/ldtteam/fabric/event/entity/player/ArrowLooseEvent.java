package com.ldtteam.fabric.event.entity.player;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class ArrowLooseEvent extends PlayerEvent implements ICancellableEvent
{
    private final net.minecraft.world.item.ItemStack bow;
    private final int charge;

    public ArrowLooseEvent(final net.minecraft.world.entity.Entity entity, final net.minecraft.world.item.ItemStack bow, final int charge)
    {
        super(entity);
        this.bow = bow;
        this.charge = charge;
    }

    public net.minecraft.world.item.ItemStack getBow()
    {
        return bow;
    }

    public int getCharge()
    {
        return charge;
    }
}
