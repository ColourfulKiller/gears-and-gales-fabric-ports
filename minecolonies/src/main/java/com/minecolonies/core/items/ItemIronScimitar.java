package com.minecolonies.core.items;

import net.minecraft.world.item.Item;

/**
 * Class handling the Scimitar item.
 */
public class ItemIronScimitar extends Item
{
    /**
     * Constructor method for the Scimitar Item
     *
     * @param properties the properties.
     */
    public ItemIronScimitar(final Item.Properties properties)
    {
        // 1.21.2: swords are components; iron durability, damage as before (wood bonus 0 + 3)
        super(properties.sword(net.minecraft.world.item.ToolMaterial.IRON, 3 - net.minecraft.world.item.ToolMaterial.IRON.attackDamageBonus(), -2.4F));
    }
}
