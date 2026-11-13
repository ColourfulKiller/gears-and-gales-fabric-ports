package com.ldtteam.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Fabric replacement for NeoForge's FMLEnvironment, shared by the LDT Team mod ports (BlockUI, Structurize,
 * Domum Ornamentum, MineColonies).
 */
public final class Env
{
    /** NeoForge: FMLEnvironment.production. True outside a development workspace. */
    public static final boolean PRODUCTION = !FabricLoader.getInstance().isDevelopmentEnvironment();

    private Env() {}

    /** NeoForge: FMLEnvironment.dist.isClient(). True on the physical client (also while hosting singleplayer). */
    public static boolean isClient()
    {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    /**
     * NeoForge: LogicalSidedProvider.WORKQUEUE.get(side).tell(task) for the physical side's side. Runs the task on the
     * client thread (physical client) or the server thread (dedicated server).
     */
    public static void runOnMainThread(final Runnable task)
    {
        if (isClient())
        {
            ClientEnv.execute(task);
            return;
        }
        final net.minecraft.server.MinecraftServer server = com.ldtteam.fabric.network.Networking.server();
        if (server != null)
        {
            server.execute(task);
        }
        else
        {
            task.run();
        }
    }

    public static boolean isModLoaded(final String modId)
    {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static String modVersion(final String modId)
    {
        return FabricLoader.getInstance().getModContainer(modId)
            .map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
    }
}
