package com.ldtteam.common.config;

import com.ldtteam.common.config.AbstractConfiguration.ConfigWatcher;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfig.Type;
import com.ldtteam.fabric.Env;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.ValueSpec;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import org.apache.commons.lang3.tuple.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Mod root configuration.
 */
public class Configurations<CLIENT extends AbstractConfiguration,
    SERVER extends AbstractConfiguration,
    COMMON extends AbstractConfiguration>
{
    /**
     * Loaded clientside, not synced
     */
    private final ModConfigSpec client;
    private final CLIENT clientConfig;

    /**
     * Loaded serverside (per world), synced on connection
     */
    private final ModConfigSpec server;
    private final SERVER serverConfig;

    /**
     * Loaded both sides, not synced
     */
    private final ModConfigSpec common;
    private final COMMON commonConfig;

    private final AbstractConfiguration[] activeConfigs;

    /**
     * Builds configuration tree.
     *
     * Fabric port: takes the mod id (NeoForge passed the ModContainer and mod event bus). Configs are registered with
     * Forge Config API Port, which provides NeoForge's config API on Fabric.
     *
     * @param modId owning mod id
     */
    public Configurations(final String modId,
        final Function<Builder, CLIENT> clientFactory,
        final Function<Builder, SERVER> serverFactory,
        final Function<Builder, COMMON> commonFactory)
    {
        final List<AbstractConfiguration> configs = new ArrayList<>();

        final Pair<CLIENT, ModConfigSpec> cli = createConfig(clientFactory, Type.CLIENT, modId, configs);
        client = cli.getRight();
        clientConfig = cli.getLeft();

        final Pair<SERVER, ModConfigSpec> ser = createConfig(serverFactory, Type.SERVER, modId, configs);
        server = ser.getRight();
        serverConfig = ser.getLeft();

        final Pair<COMMON, ModConfigSpec> com = createConfig(commonFactory, Type.COMMON, modId, configs);
        common = com.getRight();
        commonConfig = com.getLeft();

        activeConfigs = configs.toArray(AbstractConfiguration[]::new);

        // register events for watchers
        ModConfigEvents.loading(modId).register(this::onConfigLoad);
        ModConfigEvents.reloading(modId).register(this::onConfigReload);

        if (Env.isClient())
        {
            ClientConfigHelper.registerClient(modId);
        }
    }

    private <T extends AbstractConfiguration> Pair<T, ModConfigSpec> createConfig(final Function<Builder, T> factory,
        final Type type,
        final String modId,
        final List<AbstractConfiguration> configs)
    {
        // dont create client classes on server to avoid class loading issues
        if (factory == null || (type == Type.CLIENT && !Env.isClient()))
        {
            return Pair.of(null, null);
        }

        final Pair<T, ModConfigSpec> builtConfig = new ModConfigSpec.Builder().configure(factory);
        ConfigRegistry.INSTANCE.register(modId, type, builtConfig.getRight());
        final T config = builtConfig.getLeft();

        configs.add(config);

        return Pair.of(config, builtConfig.getRight());
    }

    public CLIENT getClient()
    {
        return clientConfig;
    }

    public SERVER getServer()
    {
        return serverConfig;
    }

    public COMMON getCommon()
    {
        return commonConfig;
    }

    /**
     * cache starting values for watchers
     */
    private void onConfigLoad(final ModConfig modConfig)
    {
        if (client != null && modConfig.getSpec() == client)
        {
            clientConfig.watchers.forEach(ConfigWatcher::cacheLastValue);
        }
        else if (server != null && modConfig.getSpec() == server)
        {
            serverConfig.watchers.forEach(ConfigWatcher::cacheLastValue);
        }
        else if (common != null && modConfig.getSpec() == common)
        {
            commonConfig.watchers.forEach(ConfigWatcher::cacheLastValue);
        }
    }

    /**
     * iterate watchers and fire changes if needed
     */
    private void onConfigReload(final ModConfig modConfig)
    {
        if (client != null && modConfig.getSpec() == client)
        {
            clientConfig.watchers.forEach(ConfigWatcher::compareAndFireChangeEvent);
        }
        else if (server != null && modConfig.getSpec() == server)
        {
            serverConfig.watchers.forEach(ConfigWatcher::compareAndFireChangeEvent);
        }
        else if (common != null && modConfig.getSpec() == common)
        {
            commonConfig.watchers.forEach(ConfigWatcher::compareAndFireChangeEvent);
        }
    }

    /**
     * Setter wrapper so watchers are fine. This should be called from any code that manually changes ConfigValues using set functions.
     * (Mostly done by settings UIs)
     */
    public <T> void set(final ConfigValue<T> configValue, final T value)
    {
        configValue.set(value);
        configValue.save();
        onConfigValueEdit(configValue);
    }

    /**
     * This should be called from any code that manually changes ConfigValues using set functions. (Mostly done by settings UIs)
     *
     * @param configValue which config value was changed
     */
    public void onConfigValueEdit(final ConfigValue<?> configValue)
    {
        for (final AbstractConfiguration cfg : activeConfigs)
        {
            for (final ConfigWatcher<?> configWatcher : cfg.watchers)
            {
                if (configWatcher.isSameForgeConfig(configValue))
                {
                    configWatcher.compareAndFireChangeEvent();
                }
            }
        }
    }

    /**
     * @param  value config value from this mod
     * @return       value spec, crashes in dev if not found
     */
    @Deprecated(forRemoval = true, since = "1.21")
    public Optional<ValueSpec> getSpecFromValue(final ConfigValue<?> value)
    {
        return Optional.of(value.getSpec());
    }
}
