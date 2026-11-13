package com.ldtteam.fabric.items;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** NeoForge's PlayerMainInvWrapper: the 36 main slots of a player inventory (hotbar first). */
public class PlayerMainInvWrapper extends RangedWrapper
{
    private final Inventory inventoryPlayer;

    public PlayerMainInvWrapper(final Inventory inv)
    {
        super(new InvWrapper(inv), 0, Inventory.INVENTORY_SIZE);
        this.inventoryPlayer = inv;
    }

    public Inventory getInventoryPlayer()
    {
        return inventoryPlayer;
    }

    @Override
    public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate)
    {
        final ItemStack rest = super.insertItem(slot, stack, simulate);
        if (rest.getCount() != stack.getCount() && !simulate)
        {
            final ItemStack inSlot = getStackInSlot(slot);
            if (!inSlot.isEmpty())
            {
                inSlot.setPopTime(5);
            }
        }
        return rest;
    }
}
