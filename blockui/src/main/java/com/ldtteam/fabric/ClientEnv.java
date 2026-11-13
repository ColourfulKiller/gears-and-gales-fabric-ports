package com.ldtteam.fabric;

import net.minecraft.client.Minecraft;

/**
 * Client-only half of {@link Env}; never loaded on a dedicated server.
 */
public final class ClientEnv
{
    private ClientEnv() {}

    /** Registries of the client's current level, or null outside a world. */
    public static net.minecraft.core.HolderLookup.@org.jetbrains.annotations.Nullable Provider registryAccess()
    {
        final Minecraft mc = Minecraft.getInstance();
        return mc.level == null ? null : mc.level.registryAccess();
    }

    /** The client's current level, or null outside a world. */
    public static net.minecraft.world.level.@org.jetbrains.annotations.Nullable Level level()
    {
        return Minecraft.getInstance().level;
    }

    static void execute(final Runnable task)
    {
        Minecraft.getInstance().execute(task);
    }
}
