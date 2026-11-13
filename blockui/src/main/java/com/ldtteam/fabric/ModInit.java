package com.ldtteam.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fabric runs "main" / "client" entrypoints in mod-load order, not dependency order (minecolonies sorts before
 * structurize and multipiston). NeoForge initialized dependencies first, and the LDT mods rely on that (e.g.
 * MineColonies' items use Structurize's data components). Each LDT mod opens its entrypoint with
 * {@link #begin} (run once) and calls {@link #ensure} for the mods it needs.
 */
public final class ModInit
{
    private static final Set<String> STARTED = ConcurrentHashMap.newKeySet();

    private ModInit()
    {
    }

    /** True the first time a mod's stage ("main" / "client") starts; false if it already ran. */
    public static boolean begin(final String modId, final String stage)
    {
        return STARTED.add(stage + ":" + modId);
    }

    /** Runs the given mod's entrypoint for the stage now, unless it already ran. */
    public static void ensure(final String modId, final String stage)
    {
        if (STARTED.contains(stage + ":" + modId))
        {
            return;
        }
        for (final EntrypointContainer<Object> container : FabricLoader.getInstance().getEntrypointContainers(stage, Object.class))
        {
            if (!container.getProvider().getMetadata().getId().equals(modId))
            {
                continue;
            }
            final Object entrypoint = container.getEntrypoint();
            if (entrypoint instanceof final ModInitializer init && "main".equals(stage))
            {
                init.onInitialize();
            }
            else if (entrypoint instanceof final ClientModInitializer init && "client".equals(stage))
            {
                init.onInitializeClient();
            }
        }
    }
}