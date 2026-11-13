package com.ldtteam.fabric.event;

import com.ldtteam.fabric.event.bus.Event;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;

/**
 * NeoForge's AddReloadListenerEvent (server data). Fabric registers reloaders once, so the mod posts this a single time
 * at init; listeners are reused across reloads (as the LDT ones are singletons anyway).
 */
public class AddReloadListenerEvent extends Event
{
    private final String namespace;
    private int count;

    public AddReloadListenerEvent(final String namespace)
    {
        this.namespace = namespace;
    }

    public void addListener(final PreparableReloadListener listener)
    {
        ResourceLoader.get(PackType.SERVER_DATA)
            .registerReloader(Identifier.fromNamespaceAndPath(namespace, "reload_" + (count++) + "_" + listener.getClass().getSimpleName().toLowerCase(java.util.Locale.ROOT)), listener);
    }
}