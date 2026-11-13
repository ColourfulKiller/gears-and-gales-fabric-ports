package com.ldtteam.common.config;

import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

/**
 * Client bouncer class. Fabric port: registers NeoForge's config screen (shipped by Forge Config API Port), which Mod
 * Menu opens from the mod list.
 */
public class ClientConfigHelper
{
    static void registerClient(final String modId)
    {
        ConfigScreenFactoryRegistry.INSTANCE.register(modId, ConfigurationScreen::new);
    }
}
