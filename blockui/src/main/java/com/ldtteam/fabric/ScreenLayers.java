package com.ldtteam.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Fabric replacement for NeoForge's Minecraft.pushGuiLayer / popGuiLayer (screens opened on top of another screen).
 * Vanilla only has one screen at a time, so a layer remembers the screen beneath it and restores it when closed.
 * Screens that want the look of a layer can draw {@link #below(Screen)} first.
 */
public final class ScreenLayers
{
    private static final Map<Screen, Screen> BELOW = new IdentityHashMap<>();

    private ScreenLayers() {}

    public static void push(final Screen screen)
    {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null)
        {
            BELOW.put(screen, mc.screen);
        }
        mc.setScreen(screen);
    }

    public static void pop()
    {
        final Minecraft mc = Minecraft.getInstance();
        final Screen top = mc.screen;
        final Screen below = top == null ? null : BELOW.remove(top);
        mc.setScreen(below);
    }

    public static @Nullable Screen below(final Screen screen)
    {
        return BELOW.get(screen);
    }
}
