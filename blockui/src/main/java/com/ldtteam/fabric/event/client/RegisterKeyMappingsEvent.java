package com.ldtteam.fabric.event.client;

import com.ldtteam.fabric.event.bus.Event;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

/** NeoForge's RegisterKeyMappingsEvent: straight to Fabric's key binding helper. */
public class RegisterKeyMappingsEvent extends Event
{
    public void register(final KeyMapping key)
    {
        KeyBindingHelper.registerKeyBinding(key);
    }
}