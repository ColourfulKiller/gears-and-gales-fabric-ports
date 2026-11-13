package com.ldtteam.fabric;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/**
 * Player helpers that 1.21.11 removed (they were vanilla in 1.21.1).
 */
public final class PlayerHooks
{
    private PlayerHooks()
    {
    }

    /**
     * Was {@code Player#playNotifySound}: a sound only this player hears, at their position.
     */
    public static void playNotifySound(final Player player, final SoundEvent sound, final SoundSource source, final float volume, final float pitch)
    {
        if (player instanceof final ServerPlayer serverPlayer)
        {
            serverPlayer.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
                source,
                player.getX(),
                player.getY(),
                player.getZ(),
                volume,
                pitch,
                player.getRandom().nextLong()));
        }
        else if (player.level().isClientSide())
        {
            player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), sound, source, volume, pitch, false);
        }
    }

    /** Was {@code server.getProfileCache().get(uuid)}: the server's name/id cache, as a GameProfile. */
    public static java.util.Optional<com.mojang.authlib.GameProfile> cachedProfile(final net.minecraft.server.MinecraftServer server, final java.util.UUID id)
    {
        return server.services().nameToIdCache().get(id).map(n -> new com.mojang.authlib.GameProfile(n.id(), n.name()));
    }

    /** Was {@code server.getProfileCache().get(name)}. */
    public static java.util.Optional<com.mojang.authlib.GameProfile> cachedProfile(final net.minecraft.server.MinecraftServer server, final String name)
    {
        return server.services().nameToIdCache().get(name).map(n -> new com.mojang.authlib.GameProfile(n.id(), n.name()));
    }

    /** Was {@code commandSource.hasPermission(level)} (1.21.11 permissions are sets). */
    public static boolean hasPermission(final net.minecraft.commands.CommandSourceStack source, final int level)
    {
        return source.permissions().hasPermission(new net.minecraft.server.permissions.Permission.HasCommandLevel(net.minecraft.server.permissions.PermissionLevel.byId(level)));
    }

    /** Was {@code player.hasPermissions(level)} (1.21.11 moved permissions to a PermissionSet). */
    public static boolean hasPermission(final net.minecraft.world.entity.player.Player player, final int level)
    {
        return player instanceof final net.minecraft.server.level.ServerPlayer serverPlayer && hasPermission(serverPlayer.createCommandSourceStack(), level);
    }

    /** Was NeoForge's {@code entity.isAddedToLevel()}. */
    public static boolean isAddedToLevel(final net.minecraft.world.entity.Entity entity)
    {
        return !entity.isRemoved() && entity.level().getEntity(entity.getId()) == entity;
    }
}