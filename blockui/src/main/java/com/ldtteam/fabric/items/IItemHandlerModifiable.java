package com.ldtteam.fabric.items;

import net.minecraft.world.item.ItemStack;

/** NeoForge's IItemHandlerModifiable: an item handler whose slots can be set directly. */
public interface IItemHandlerModifiable extends IItemHandler
{
    void setStackInSlot(int slot, ItemStack stack);
}