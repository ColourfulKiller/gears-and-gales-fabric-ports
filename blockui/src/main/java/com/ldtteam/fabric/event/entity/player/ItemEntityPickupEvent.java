package com.ldtteam.fabric.event.entity.player;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class ItemEntityPickupEvent extends Event
{
    private final net.minecraft.world.entity.player.Player player;
    private final net.minecraft.world.entity.item.ItemEntity itemEntity;

    public ItemEntityPickupEvent(final net.minecraft.world.entity.player.Player player, final net.minecraft.world.entity.item.ItemEntity itemEntity)
    {
        this.player = player;
        this.itemEntity = itemEntity;
    }

    public net.minecraft.world.entity.player.Player getPlayer()
    {
        return player;
    }

    public net.minecraft.world.entity.item.ItemEntity getItemEntity()
    {
        return itemEntity;
    }

    public static class Pre extends ItemEntityPickupEvent
    {

        public Pre(final net.minecraft.world.entity.player.Player player, final net.minecraft.world.entity.item.ItemEntity itemEntity)
        {
            super(player, itemEntity);
        }

    private net.fabricmc.fabric.api.util.TriState canPickup = net.fabricmc.fabric.api.util.TriState.DEFAULT;

    public void setCanPickup(final net.fabricmc.fabric.api.util.TriState canPickup)
    {
        this.canPickup = canPickup;
    }

    public net.fabricmc.fabric.api.util.TriState canPickup()
    {
        return canPickup;
    }

    }
}
