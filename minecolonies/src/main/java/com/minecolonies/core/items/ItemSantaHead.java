package com.minecolonies.core.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.core.Holder;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

/**
 * Class handling the Santa hat.
 */
public class ItemSantaHead extends Item
{
    /**
     * Constructor method for the Chief Sword Item
     *
     * @param name            the name.
     * @param materialIn      the material.
     * @param equipmentSlotIn the equipment slot.
     * @param properties      the item properties.
     */
    public ItemSantaHead(
      @NotNull final String name,
      @NotNull final ArmorMaterial materialIn,
      @NotNull final ArmorType equipmentSlotIn,
      final Item.Properties properties)
    {
        // 1.21.4: armor is a plain item; the caller applies properties.humanoidArmor(material, type)
        super(properties);
    }
}
