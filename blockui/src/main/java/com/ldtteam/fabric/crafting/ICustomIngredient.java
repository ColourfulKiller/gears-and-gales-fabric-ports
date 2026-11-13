package com.ldtteam.fabric.crafting;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Stream;

/**
 * NeoForge's ICustomIngredient surface on Fabric's CustomIngredient: implement test / getItems / isSimple / getType
 * as before; the Fabric methods are derived. {@link #toVanilla()} gives the Ingredient.
 */
public interface ICustomIngredient extends CustomIngredient
{
    @Override
    boolean test(ItemStack stack);

    /** The stacks this ingredient shows / matches (NeoForge's getItems). */
    Stream<ItemStack> getItems();

    /** Simple ingredients match purely by item (no per-stack test needed). */
    boolean isSimple();

    IngredientType<?> getType();

    @Override
    default Stream<Holder<Item>> getMatchingItems()
    {
        return getItems().map(ItemStack::getItemHolder).distinct();
    }

    @Override
    default boolean requiresTesting()
    {
        return !isSimple();
    }

    @Override
    default CustomIngredientSerializer<?> getSerializer()
    {
        return getType().serializer();
    }
}