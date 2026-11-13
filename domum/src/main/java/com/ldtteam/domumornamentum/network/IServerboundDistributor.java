package com.ldtteam.domumornamentum.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.ldtteam.fabric.network.Networking;

/**
 * List of possible network targets when sending from client to server.
 */
public interface IServerboundDistributor extends CustomPacketPayload
{
    public default void sendToServer()
    {
        Networking.sendToServer(this);
    }
}
