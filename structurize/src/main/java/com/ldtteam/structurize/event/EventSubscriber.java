package com.ldtteam.structurize.event;

import com.ldtteam.structurize.commands.EntryPoint;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.storage.ServerFutureProcessor;
import com.ldtteam.structurize.storage.ServerStructurePackLoader;
import com.ldtteam.structurize.storage.rendering.ServerPreviewDistributor;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.IOPool;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

/**
 * Common (both sides) game events, wired to Fabric callbacks (was the NeoForge game-bus subscriber).
 */
public class EventSubscriber
{
    private EventSubscriber()
    {
        /*
         * Intentionally left empty
         */
    }

    public static void register()
    {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> EntryPoint.register(dispatcher, environment));

        ServerTickEvents.START_WORLD_TICK.register(level -> {
            BlockUtils.checkOrInit();
            Manager.onWorldTick(level);
        });
        ServerTickEvents.END_WORLD_TICK.register(ServerFutureProcessor::onWorldTick);
        ServerTickEvents.END_SERVER_TICK.register(ServerStructurePackLoader::onWorldTick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> ServerPreviewDistributor.onLogout(handler.player));

        // the NeoForge version only did this on a dedicated server during mod loading; the integrated server's
        // start is driven by the client (ClientStructurePackLoader)
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            if (server.isDedicatedServer())
            {
                ServerStructurePackLoader.onServerStarting();
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> IOPool.shutdown());
    }
}