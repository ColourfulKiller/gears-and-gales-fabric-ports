package com.ldtteam.fabric.event;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class LootTableLoadEvent extends Event
{
    private final net.minecraft.resources.Identifier name;
    private final TableAccess table;

    public LootTableLoadEvent(final net.minecraft.resources.Identifier name, final TableAccess table)
    {
        this.name = name;
        this.table = table;
    }

    public net.minecraft.resources.Identifier getName()
    {
        return name;
    }

    public TableAccess getTable()
    {
        return table;
    }

    /** What handlers used of NeoForge's mutable LootTable: adding pools (to Fabric's builder). */
    public record TableAccess(net.minecraft.world.level.storage.loot.LootTable.Builder builder)
    {
        public void addPool(final net.minecraft.world.level.storage.loot.LootPool pool)
        {
            builder.pool(pool);
        }
    }
}
