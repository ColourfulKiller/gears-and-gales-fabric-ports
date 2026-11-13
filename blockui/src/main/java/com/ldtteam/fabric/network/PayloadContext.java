package com.ldtteam.fabric.network;

import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric replacement for NeoForge's IPayloadContext: what a message handler gets besides the message itself.
 * Fabric runs play-payload receivers on the main thread, so {@link #enqueueWork} runs the task straight away.
 */
public interface PayloadContext
{
    /** The receiving player (client: the local player, server: the sender). */
    @Nullable
    Player player();

    /** CLIENTBOUND when handled on the client, SERVERBOUND when handled on the server. */
    PacketFlow flow();

    void enqueueWork(Runnable task);

    static PayloadContext of(@Nullable final Player player, final PacketFlow flow)
    {
        return new PayloadContext()
        {
            @Override
            public @Nullable Player player()
            {
                return player;
            }

            @Override
            public PacketFlow flow()
            {
                return flow;
            }

            @Override
            public void enqueueWork(final Runnable task)
            {
                task.run();
            }
        };
    }
}
