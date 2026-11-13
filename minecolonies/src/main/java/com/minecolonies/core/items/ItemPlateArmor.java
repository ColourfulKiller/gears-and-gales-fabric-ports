package com.minecolonies.core.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.core.Holder;
import net.minecraft.world.item.equipment.ArmorMaterial;
import org.jetbrains.annotations.NotNull;

/**
 * Class handling the Plate Armor.
 */
public class ItemPlateArmor extends Item
{
    /**
     * Constructor method for the Plate Armor
     *
     * @param properties      the item properties.
     * @param equipmentSlotIn the equipment slot of it.
     * @param materialIn      the material of the armour.
     * @param name            the name.
     */
    public ItemPlateArmor(
      @NotNull final String name,
      @NotNull final ArmorMaterial materialIn,
      @NotNull final ArmorType equipmentSlotIn,
      final Properties properties)
    {
        // 1.21.4: armor is a plain item; the caller applies properties.humanoidArmor(material, type)
        super(properties);
    }
}
