package com.ldtteam.fabric.event.client;

import com.ldtteam.fabric.event.bus.Event;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;

/** NeoForge's RegisterClientReloadListenersEvent: straight to Fabric's client resource reloaders. */
public class RegisterClientReloadListenersEvent extends Event
{
    private final String namespace;
    private int count;

    public RegisterClientReloadListenersEvent(final String namespace)
    {
        this.namespace = namespace;
    }

    public void registerReloadListener(final PreparableReloadListener listener)
    {
        ResourceLoader.get(PackType.CLIENT_RESOURCES)
            .registerReloader(Identifier.fromNamespaceAndPath(namespace, "client_reload_" + (count++) + "_" + listener.getClass().getSimpleName().toLowerCase(java.util.Locale.ROOT)), listener);
    }
}