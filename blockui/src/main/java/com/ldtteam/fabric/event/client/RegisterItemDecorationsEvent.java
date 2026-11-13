package com.ldtteam.fabric.event.client;

import com.ldtteam.fabric.event.bus.Event;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** NeoForge's RegisterItemDecorationsEvent: extra overlays drawn over an item's slot icon (see GuiGraphicsDecorationsMixin). */
public class RegisterItemDecorationsEvent extends Event
{
    /** NeoForge's IItemDecorator. @return whether anything was drawn */
    @FunctionalInterface
    public interface IItemDecorator
    {
        boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset);
    }

    private static final Map<Item, List<IItemDecorator>> DECORATORS = new ConcurrentHashMap<>();

    public void register(final ItemLike item, final IItemDecorator decorator)
    {
        DECORATORS.computeIfAbsent(item.asItem(), k -> new CopyOnWriteArrayList<>()).add(decorator);
    }

    public static List<IItemDecorator> decorators(final Item item)
    {
        return DECORATORS.getOrDefault(item, List.of());
    }
}