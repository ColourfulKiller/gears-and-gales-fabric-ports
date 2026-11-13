package com.ldtteam.fabric.network;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric replacement for NeoForge's PacketDistributor, shared by the LDT Team mod ports.
 * Call {@link #init()} once from a main entrypoint (it tracks the running server for {@link #sendToAllPlayers}).
 */
public final class Networking
{
    private static @Nullable MinecraftServer server;
    private static boolean initialised;

    private Networking() {}

    public static synchronized void init()
    {
        if (initialised)
        {
            return;
        }
        initialised = true;
        ServerLifecycleEvents.SERVER_STARTING.register(s -> server = s);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> server = null);
    }

    /** The running (integrated or dedicated) server, or null on a client that isn't hosting one. */
    public static @Nullable MinecraftServer server()
    {
        return server;
    }

    public static void sendToPlayer(final ServerPlayer player, final CustomPacketPayload payload)
    {
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendToPlayersInDimension(final ServerLevel level, final CustomPacketPayload payload)
    {
        PlayerLookup.world(level).forEach(p -> ServerPlayNetworking.send(p, payload));
    }

    public static void sendToPlayersNear(final ServerLevel level,
        @Nullable final ServerPlayer excluded,
        final double x,
        final double y,
        final double z,
        final double radius,
        final CustomPacketPayload payload)
    {
        PlayerLookup.around(level, new Vec3(x, y, z), radius).forEach(p -> {
            if (p != excluded)
            {
                ServerPlayNetworking.send(p, payload);
            }
        });
    }

    public static void sendToAllPlayers(final CustomPacketPayload payload)
    {
        if (server != null)
        {
            PlayerLookup.all(server).forEach(p -> ServerPlayNetworking.send(p, payload));
        }
    }

    public static void sendToPlayersTrackingEntity(final Entity entity, final CustomPacketPayload payload)
    {
        PlayerLookup.tracking(entity).forEach(p -> ServerPlayNetworking.send(p, payload));
    }

    public static void sendToPlayersTrackingEntityAndSelf(final Entity entity, final CustomPacketPayload payload)
    {
        sendToPlayersTrackingEntity(entity, payload);
        if (entity instanceof final ServerPlayer self)
        {
            ServerPlayNetworking.send(self, payload);
        }
    }

    public static void sendToPlayersTrackingChunk(final ServerLevel level, final ChunkPos chunk, final CustomPacketPayload payload)
    {
        PlayerLookup.tracking(level, chunk).forEach(p -> ServerPlayNetworking.send(p, payload));
    }

    /** Client -> server. Only call on the client. */
    public static void sendToServer(final CustomPacketPayload payload)
    {
        ClientNetworking.sendToServer(payload);
    }
}
