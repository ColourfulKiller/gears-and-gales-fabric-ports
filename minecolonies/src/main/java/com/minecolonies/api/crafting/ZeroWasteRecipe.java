package com.minecolonies.api.crafting;

import com.ldtteam.fabric.crafting.RecipeHooks;
import com.minecolonies.api.crafting.registry.ModRecipeSerializer;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * A shapeless recipe that discards any remaining items.  Mainly intended for mixing things into bottles or bowls
 * without leaving extra empties behind, but can be used for other things too.
 * <p>
 * Fabric 1.21.11 port: the datagen-only recipe builder was dropped (the recipe JSON ships in the resources).
 */
public class ZeroWasteRecipe extends ShapelessRecipe
{
    public ZeroWasteRecipe(@NotNull final ItemStack output, @NotNull final List<Ingredient> inputs)
    {
        super("", CraftingBookCategory.MISC, output, inputs);
    }

    @NotNull
    @Override
    public NonNullList<ItemStack> getRemainingItems(@NotNull final CraftingInput input)
    {
        final NonNullList<ItemStack> remainingItems = super.getRemainingItems(input);
        Collections.fill(remainingItems, ItemStack.EMPTY);
        return remainingItems;
    }

    @NotNull
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RecipeSerializer<ShapelessRecipe> getSerializer()
    {
        return (RecipeSerializer) ModRecipeSerializer.ZeroWasteRecipeSerializer.get();
    }

    public static class Serializer implements RecipeSerializer<ZeroWasteRecipe>
    {
        private static final MapCodec<ZeroWasteRecipe> CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> RecipeHooks.getResultItem(r, null)),
                        Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(RecipeHooks::getIngredients)
                ).apply(builder, ZeroWasteRecipe::new)
        );
        private static final StreamCodec<RegistryFriendlyByteBuf, ZeroWasteRecipe> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, r -> RecipeHooks.getResultItem(r, null),
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), RecipeHooks::getIngredients,
                ZeroWasteRecipe::new
        );

        @NotNull
        @Override
        public MapCodec<ZeroWasteRecipe> codec()
        {
            return CODEC;
        }

        @NotNull
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ZeroWasteRecipe> streamCodec()
        {
            return STREAM_CODEC;
        }
    }
}
