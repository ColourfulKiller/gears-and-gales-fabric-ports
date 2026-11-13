package com.ldtteam.blockui.mod;

import com.ldtteam.fabric.network.Networking;
import net.fabricmc.api.ModInitializer;

/**
 * Fabric main entrypoint (was the NeoForge @Mod class). BlockUI has no server-side content of its own; this sets up
 * the shared networking helper that ports built on BlockUI's com.ldtteam.common package use.
 */
public class BlockUI implements ModInitializer
{
    public static final String MOD_ID = "blockui";

    @Override
    public void onInitialize()
    {
        if (!com.ldtteam.fabric.ModInit.begin("blockui", "main"))
        {
            return;
        }
        Networking.init();
        // NeoForge-shaped events for the LDT mods (com.ldtteam.fabric.event)
        com.ldtteam.fabric.event.EventBridge.init();
        com.ldtteam.fabric.data.GlobalLootModifiers.init();
    }
}
