package com.ldtteam.domumornamentum.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import com.ldtteam.fabric.Env;
import com.ldtteam.fabric.network.Networking;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;

/**
 * List of possible network targets when sending from server to client.
 */
public interface IClientboundDistributor extends CustomPacketPayload
{
    /**
     * @see #sendToPlayer(ServerPlayer)
     */
    public default void sendToPlayer(final Collection<ServerPlayer> players)
    {
        for (final ServerPlayer serverPlayer : players)
        {
            sendToPlayer(serverPlayer);
        }
    }

    public default void sendToPlayer(final ServerPlayer player)
    {
        Networking.sendToPlayer(player, this);
    }

    public default void sendToDimension(final ServerLevel serverLevel)
    {
        Networking.sendToPlayersInDimension(serverLevel, this);
    }

    public default void sendToTargetPoint(final ServerLevel level,
        @Nullable final ServerPlayer excluded,
        final double x,
        final double y,
        final double z,
        final double radius)
    {
        Networking.sendToPlayersNear(level, excluded, x, y, z, radius, this);
    }

    public default void sendToAllClients()
    {
        Networking.sendToAllPlayers(this);
    }

    public default void sendToTrackingEntity(final Entity entity)
    {
        Networking.sendToPlayersTrackingEntity(entity, this);
    }

    public default void sendToTrackingEntityAndSelf(final Entity entity)
    {
        Networking.sendToPlayersTrackingEntityAndSelf(entity, this);
    }

    public default void sendToPlayersTrackingChunk(final LevelChunk chunk)
    {
        if (chunk.getLevel() instanceof final ServerLevel level)
        {
            Networking.sendToPlayersTrackingChunk(level, chunk.getPos(), this);
            return;
        }

        final String crash =
            "Got client chunk for server network message: " + this.getClass().getName() + " - " + chunk.getClass().getName();
        if (Env.PRODUCTION)
        {
            new IllegalArgumentException(crash).printStackTrace();
        }
        else
        {
            throw new IllegalArgumentException(crash);
        }
    }

    public default void sendToPlayersTrackingChunk(final ServerLevel serverLevel, final ChunkPos chunkPos)
    {
        Networking.sendToPlayersTrackingChunk(serverLevel, chunkPos, this);
    }
}