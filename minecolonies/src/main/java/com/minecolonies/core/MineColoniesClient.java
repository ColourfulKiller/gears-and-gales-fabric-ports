package com.minecolonies.core;

import com.ldtteam.fabric.event.NeoForge;
import com.ldtteam.fabric.event.bus.EventBus;
import com.ldtteam.fabric.event.client.EntityRenderersEvent;
import com.ldtteam.fabric.event.client.RegisterClientReloadListenersEvent;
import com.ldtteam.fabric.event.client.RegisterKeyMappingsEvent;
import com.ldtteam.fabric.event.client.RegisterMenuScreensEvent;
import com.ldtteam.fabric.event.client.RegisterParticleProvidersEvent;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.initializer.ModContainerInitializers;
import com.minecolonies.apiimp.initializer.ModParticleTypesInitializer;
import com.minecolonies.core.event.ClientEventHandler;
import com.minecolonies.core.event.ClientRegistryHandler;
import com.minecolonies.core.event.ColonyStoryListener;
import com.minecolonies.core.event.DataPackSyncEventHandler;
import com.minecolonies.core.event.TextureReloadListener;
import net.fabricmc.api.ClientModInitializer;

/**
 * Fabric client entrypoint: the client half of the NeoForge mod constructor (client game-bus handlers, blueprint tag
 * options) and the client mod-bus registration events, posted on a client mod bus.
 */
public class MineColoniesClient implements ClientModInitializer
{
    public static final EventBus CLIENT_MOD_BUS = new EventBus();

    @Override
    public void onInitializeClient()
    {
        if (!com.ldtteam.fabric.ModInit.begin("minecolonies", "client"))
        {
            return;
        }
        com.ldtteam.fabric.ModInit.ensure("blockui", "client");
        com.ldtteam.fabric.ModInit.ensure("domum_ornamentum", "client");
        com.ldtteam.fabric.ModInit.ensure("structurize", "client");
        com.ldtteam.fabric.ModInit.ensure("multipiston", "client");
        MineColonies.registerClientMessages();
        MineColonies.registerTagOptions();

        NeoForge.EVENT_BUS.register(ClientEventHandler.class);
        NeoForge.EVENT_BUS.register(DataPackSyncEventHandler.ClientEvents.class);

        CLIENT_MOD_BUS.register(ClientRegistryHandler.class);
        CLIENT_MOD_BUS.register(ModContainerInitializers.class);
        CLIENT_MOD_BUS.register(ModParticleTypesInitializer.ClientRegistration.class);
        CLIENT_MOD_BUS.register(ColonyStoryListener.class);
        CLIENT_MOD_BUS.register(TextureReloadListener.class);

        CLIENT_MOD_BUS.post(new RegisterMenuScreensEvent());
        CLIENT_MOD_BUS.post(new RegisterParticleProvidersEvent());
        CLIENT_MOD_BUS.post(new EntityRenderersEvent.RegisterLayerDefinitions());
        CLIENT_MOD_BUS.post(new EntityRenderersEvent.RegisterRenderers());
        CLIENT_MOD_BUS.post(new RegisterKeyMappingsEvent());
        CLIENT_MOD_BUS.post(new RegisterClientReloadListenersEvent(Constants.MOD_ID));
        CLIENT_MOD_BUS.post(new com.ldtteam.fabric.event.client.RegisterItemDecorationsEvent());

        com.minecolonies.core.client.render.SpearItemTileEntityRenderer.register();
        com.minecolonies.core.client.render.GogglesDisabledProperty.register();
        com.ldtteam.structurize.client.render.RenderLevelStage.EVENT.register(ClientEventHandler::renderWorldLastEvent);
    }
}