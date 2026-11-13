package com.ldtteam.fabric.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 1.21.2 dropped Recipe#getResultItem, #getIngredients and #canCraftInDimensions, and RecipeManager#getAllRecipesFor.
 * These rebuild them for the vanilla recipe classes, falling back to the recipe's display for anything else.
 * Recipes only exist on the server since 1.21.2.
 */
public final class RecipeHooks
{
    private RecipeHooks()
    {
    }

    /** Was {@code recipeManager.getAllRecipesFor(type)}. */
    @SuppressWarnings("unchecked")
    public static <T extends Recipe<?>> List<RecipeHolder<T>> getAllRecipesFor(final RecipeManager manager, final RecipeType<T> type)
    {
        final List<RecipeHolder<T>> out = new ArrayList<>();
        for (final RecipeHolder<?> holder : manager.getRecipes())
        {
            if (holder.value().getType() == type)
            {
                out.add((RecipeHolder<T>) holder);
            }
        }
        return out;
    }

    /** Was {@code recipeManager.getRecipesFor(type, input, level)}: every matching recipe. */
    @SuppressWarnings("unchecked")
    public static <I extends net.minecraft.world.item.crafting.RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> getRecipesFor(
      final RecipeManager manager, final RecipeType<T> type, final I input, final net.minecraft.world.level.Level level)
    {
        final List<RecipeHolder<T>> out = new ArrayList<>();
        for (final RecipeHolder<T> holder : getAllRecipesFor(manager, type))
        {
            if (holder.value().matches(input, level))
            {
                out.add(holder);
            }
        }
        return out;
    }

    /** Was {@code recipe.getResultItem(registries)}: a copy of the output stack. */
    public static ItemStack getResultItem(final Recipe<?> recipe, @Nullable final HolderLookup.Provider registries)
    {
        if (recipe instanceof final ShapedRecipe shaped)
        {
            return shaped.result.copy();
        }
        if (recipe instanceof final ShapelessRecipe shapeless)
        {
            return shapeless.result.copy();
        }
        if (recipe instanceof final SingleItemRecipe single)
        {
            return single.result().copy();
        }
        final ContextMap.Builder context = new ContextMap.Builder();
        if (registries != null)
        {
            context.withParameter(SlotDisplayContext.REGISTRIES, registries);
        }
        final ContextMap map = context.create(SlotDisplayContext.CONTEXT);
        for (final RecipeDisplay display : recipe.display())
        {
            final ItemStack stack = display.result().resolveForFirstStack(map);
            if (!stack.isEmpty())
            {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Was {@code recipe.getIngredients()}, with the shaped grid's empty cells left out (1.21.2 has no empty ingredient).
     */
    public static List<Ingredient> getIngredients(final Recipe<?> recipe)
    {
        if (recipe instanceof final ShapedRecipe shaped)
        {
            return shaped.getIngredients().stream().flatMap(Optional::stream).toList();
        }
        if (recipe instanceof final ShapelessRecipe shapeless)
        {
            return shapeless.ingredients;
        }
        if (recipe instanceof final SingleItemRecipe single)
        {
            return List.of(single.input());
        }
        return recipe.placementInfo().isImpossibleToPlace() ? List.of() : recipe.placementInfo().ingredients();
    }

    /** The shaped grid in row-major order, empty cells as {@link Optional#empty()}. */
    public static List<Optional<Ingredient>> getGrid(final Recipe<?> recipe)
    {
        if (recipe instanceof final ShapedRecipe shaped)
        {
            return shaped.getIngredients();
        }
        return getIngredients(recipe).stream().map(Optional::of).toList();
    }

    /** Was {@code recipe.canCraftInDimensions(width, height)}. */
    public static boolean canCraftInDimensions(final Recipe<?> recipe, final int width, final int height)
    {
        if (recipe instanceof final ShapedRecipe shaped)
        {
            return shaped.getWidth() <= width && shaped.getHeight() <= height;
        }
        return getIngredients(recipe).size() <= width * height;
    }

    /** All recipes of the given types. */
    public static List<RecipeHolder<?>> byTypes(final RecipeManager manager, final Collection<RecipeType<?>> types)
    {
        return manager.getRecipes().stream().filter(h -> types.contains(h.value().getType())).toList();
    }
}
