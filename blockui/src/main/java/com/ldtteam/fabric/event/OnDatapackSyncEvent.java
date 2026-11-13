package com.ldtteam.fabric.event;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class OnDatapackSyncEvent extends Event
{
    private final net.minecraft.server.players.PlayerList playerList;
    private final net.minecraft.server.level.ServerPlayer player;

    public OnDatapackSyncEvent(final net.minecraft.server.players.PlayerList playerList, final net.minecraft.server.level.ServerPlayer player)
    {
        this.playerList = playerList;
        this.player = player;
    }

    public net.minecraft.server.players.PlayerList getPlayerList()
    {
        return playerList;
    }

    public net.minecraft.server.level.ServerPlayer getPlayer()
    {
        return player;
    }
}
