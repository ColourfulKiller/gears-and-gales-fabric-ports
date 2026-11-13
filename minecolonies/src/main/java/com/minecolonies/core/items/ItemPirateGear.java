package com.minecolonies.core.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.core.Holder;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;

/**
 * Class handling the Pirate Gear.
 */
public class ItemPirateGear extends Item
{
    /**
     * Constructor method for the Pirate Gear
     *
     * @param properties      the item properties.
     * @param equipmentSlotIn the equipment slot of it.
     * @param materialIn      the material of the armour.
     * @param name            the name.
     */
    public ItemPirateGear(
      @NotNull final String name,
      @NotNull final ArmorMaterial materialIn,
      @NotNull final ArmorType equipmentSlotIn,
      final Item.Properties properties)
    {
        // 1.21.4: armor is a plain item; the caller applies properties.humanoidArmor(material, type)
        super(properties);
    }
}
