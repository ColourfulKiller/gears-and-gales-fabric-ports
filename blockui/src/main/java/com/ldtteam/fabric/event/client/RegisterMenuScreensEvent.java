package com.ldtteam.fabric.event.client;

import com.ldtteam.fabric.event.bus.Event;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

/** NeoForge's RegisterMenuScreensEvent: straight to MenuScreens. */
public class RegisterMenuScreensEvent extends Event
{
    public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(final MenuType<? extends M> type, final MenuScreens.ScreenConstructor<M, U> factory)
    {
        MenuScreens.register(type, factory);
    }
}