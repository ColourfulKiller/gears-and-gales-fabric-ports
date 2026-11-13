package com.ldtteam.fabric.event.entity.player;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class ItemTooltipEvent extends PlayerEvent
{
    private final net.minecraft.world.item.ItemStack itemStack;
    private final java.util.List<net.minecraft.network.chat.Component> toolTip;
    private final net.minecraft.world.item.TooltipFlag flags;

    public ItemTooltipEvent(final net.minecraft.world.entity.Entity entity, final net.minecraft.world.item.ItemStack itemStack, final java.util.List<net.minecraft.network.chat.Component> toolTip, final net.minecraft.world.item.TooltipFlag flags)
    {
        super(entity);
        this.itemStack = itemStack;
        this.toolTip = toolTip;
        this.flags = flags;
    }

    public net.minecraft.world.item.ItemStack getItemStack()
    {
        return itemStack;
    }

    public java.util.List<net.minecraft.network.chat.Component> getToolTip()
    {
        return toolTip;
    }

    public net.minecraft.world.item.TooltipFlag getFlags()
    {
        return flags;
    }
}
