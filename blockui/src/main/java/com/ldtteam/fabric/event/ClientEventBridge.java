package com.ldtteam.fabric.event;

import com.ldtteam.fabric.event.entity.player.ItemTooltipEvent;
import com.ldtteam.fabric.event.tick.LevelTickEvent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.Minecraft;

/** Client-side half of {@link EventBridge}: client world ticks and item tooltips. */
public final class ClientEventBridge
{
    private static boolean initialized;

    private ClientEventBridge()
    {
    }

    public static synchronized void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;
        ClientTickEvents.START_CLIENT_TICK.register(mc -> EventBridge.post(new com.ldtteam.fabric.event.client.ClientTickEvent.Pre()));
        ClientTickEvents.END_CLIENT_TICK.register(mc -> EventBridge.post(new com.ldtteam.fabric.event.client.ClientTickEvent.Post()));
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((handler, sender, mc) -> EventBridge.post(new com.ldtteam.fabric.event.client.ClientPlayerNetworkEvent.LoggingIn()));
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, mc) -> EventBridge.post(new com.ldtteam.fabric.event.client.ClientPlayerNetworkEvent.LoggingOut()));
        ClientTickEvents.START_WORLD_TICK.register(level -> EventBridge.post(new LevelTickEvent.Pre(level)));
        ClientTickEvents.END_WORLD_TICK.register(level -> EventBridge.post(new LevelTickEvent.Post(level)));
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            if (EventBridge.wants(ItemTooltipEvent.class))
            {
                EventBridge.post(new ItemTooltipEvent(Minecraft.getInstance().player, stack, lines, flag));
            }
        });
    }
}