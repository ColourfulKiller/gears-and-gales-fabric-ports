package com.ldtteam.fabric;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** 1.21.2 turned Ingredient into a holder set; these keep the ItemStack-array view the LDT code used. */
public final class IngredientHooks
{
    private IngredientHooks()
    {
    }

    /** Was {@code ingredient.getItems()}. */
    public static ItemStack[] getItems(final Ingredient ingredient)
    {
        return ingredient.items().map(ItemStack::new).toArray(ItemStack[]::new);
    }
}