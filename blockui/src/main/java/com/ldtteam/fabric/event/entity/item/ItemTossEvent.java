package com.ldtteam.fabric.event.entity.item;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class ItemTossEvent extends com.ldtteam.fabric.event.entity.EntityEvent implements ICancellableEvent
{
    private final net.minecraft.world.entity.player.Player player;

    public ItemTossEvent(final net.minecraft.world.entity.Entity entity, final net.minecraft.world.entity.player.Player player)
    {
        super(entity);
        this.player = player;
    }

    public net.minecraft.world.entity.player.Player getPlayer()
    {
        return player;
    }

    @Override
    public net.minecraft.world.entity.item.ItemEntity getEntity()
    {
        return (net.minecraft.world.entity.item.ItemEntity) super.getEntity();
    }
}
