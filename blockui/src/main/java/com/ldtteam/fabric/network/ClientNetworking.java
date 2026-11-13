package com.ldtteam.fabric.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.BiConsumer;

/**
 * Client-only networking calls, kept in their own class so code that runs on a dedicated server never references
 * ClientPlayNetworking (Fabric strips client-only classes from servers). Only call these on the client.
 */
public final class ClientNetworking
{
    private ClientNetworking() {}

    public static void sendToServer(final CustomPacketPayload payload)
    {
        ClientPlayNetworking.send(payload);
    }

    public static <T extends CustomPacketPayload> void registerReceiver(final CustomPacketPayload.Type<T> type,
        final BiConsumer<T, PayloadContext> handler)
    {
        ClientPlayNetworking.registerGlobalReceiver(type,
            (payload, ctx) -> handler.accept(payload, PayloadContext.of(ctx.player(), PacketFlow.CLIENTBOUND)));
    }
}
