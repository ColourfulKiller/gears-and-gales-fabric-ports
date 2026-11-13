package com.ldtteam.fabric.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

/**
 * Fabric replacement for NeoForge's DeferredBlock.
 */
public class DeferredBlock<T extends Block> extends DeferredHolder<Block, T> implements ItemLike
{
    protected DeferredBlock(final ResourceKey<Block> key)
    {
        super(key);
    }

    public static <T extends Block> DeferredBlock<T> createBlock(final Identifier id)
    {
        return new DeferredBlock<>(ResourceKey.create(Registries.BLOCK, id));
    }

    @Override
    public Item asItem()
    {
        return get().asItem();
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
