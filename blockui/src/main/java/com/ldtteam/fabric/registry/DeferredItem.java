package com.ldtteam.fabric.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Fabric replacement for NeoForge's DeferredItem.
 */
public class DeferredItem<T extends Item> extends DeferredHolder<Item, T> implements ItemLike
{
    protected DeferredItem(final ResourceKey<Item> key)
    {
        super(key);
    }

    public static <T extends Item> DeferredItem<T> createItem(final Identifier id)
    {
        return new DeferredItem<>(ResourceKey.create(Registries.ITEM, id));
    }

    @Override
    public Item asItem()
    {
        return get();
    }

    public ItemStack toStack()
    {
        return new ItemStack(this);
    }

    public ItemStack toStack(final int count)
    {
        return new ItemStack(this, count);
    }
}
