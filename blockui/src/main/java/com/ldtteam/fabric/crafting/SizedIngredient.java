package com.ldtteam.fabric.crafting;

import com.ldtteam.fabric.IngredientHooks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * NeoForge's SizedIngredient: an ingredient plus a count.
 * <p>
 * {@link #FLAT_CODEC} reads the old flat JSON shape ({@code {"item": "minecraft:book", "count": 2}},
 * {@code {"tag": "minecraft:logs", "count": 4}}, or {@code {"items": [...]}}) and writes the same.
 */
public record SizedIngredient(Ingredient ingredient, int count)
{
    public static final MapCodec<SizedIngredient> FLAT_MAP_CODEC = new MapCodec<>()
    {
        @Override
        public <T> Stream<T> keys(final DynamicOps<T> ops)
        {
            return Stream.of("item", "tag", "items", "count").map(ops::createString);
        }

        @Override
        public <T> DataResult<SizedIngredient> decode(final DynamicOps<T> ops, final MapLike<T> input)
        {
            final int count = input.get("count") == null ? 1 : ops.getNumberValue(input.get("count")).map(Number::intValue).result().orElse(1);
            final DataResult<Ingredient> ingredient;
            if (input.get("tag") != null)
            {
                ingredient = ops.getStringValue(input.get("tag"))
                    .flatMap(tag -> Ingredient.CODEC.parse(ops, ops.createString(tag.startsWith("#") ? tag : "#" + tag)));
            }
            else if (input.get("item") != null)
            {
                ingredient = Ingredient.CODEC.parse(ops, input.get("item"));
            }
            else if (input.get("items") != null)
            {
                ingredient = Ingredient.CODEC.parse(ops, input.get("items"));
            }
            else
            {
                return DataResult.error(() -> "Sized ingredient needs one of item / tag / items");
            }
            return ingredient.map(i -> new SizedIngredient(i, count));
        }

        @Override
        public <T> RecordBuilder<T> encode(final SizedIngredient input, final DynamicOps<T> ops, final RecordBuilder<T> prefix)
        {
            final DataResult<T> encoded = Ingredient.CODEC.encodeStart(ops, input.ingredient());
            final T value = encoded.result().orElse(null);
            if (value != null)
            {
                final String str = ops.getStringValue(value).result().orElse(null);
                if (str != null && str.startsWith("#"))
                {
                    prefix.add("tag", ops.createString(str.substring(1)));
                }
                else if (str != null)
                {
                    prefix.add("item", value);
                }
                else
                {
                    prefix.add("items", value);
                }
            }
            return prefix.add("count", ops.createInt(input.count()));
        }
    };

    public static final Codec<SizedIngredient> FLAT_CODEC = FLAT_MAP_CODEC.codec();

    /** Nested shape: {@code {"ingredient": ..., "count": n}}. */
    public static final Codec<SizedIngredient> NESTED_CODEC = RecordCodecBuilder.create(i -> i.group(
        Ingredient.CODEC.fieldOf("ingredient").forGetter(SizedIngredient::ingredient),
        Codec.INT.optionalFieldOf("count", 1).forGetter(SizedIngredient::count)).apply(i, SizedIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SizedIngredient> STREAM_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, SizedIngredient::ingredient,
        ByteBufCodecs.VAR_INT, SizedIngredient::count,
        SizedIngredient::new);

    public static SizedIngredient of(final net.minecraft.world.level.ItemLike item, final int count)
    {
        return new SizedIngredient(Ingredient.of(item), count);
    }

    public boolean test(final ItemStack stack)
    {
        return ingredient.test(stack) && stack.getCount() >= count;
    }

    /** The matching stacks, each at this ingredient's count. */
    public ItemStack[] getItems()
    {
        return Arrays.stream(IngredientHooks.getItems(ingredient)).map(s -> s.copyWithCount(count)).toArray(ItemStack[]::new);
    }

    @Override
    public String toString()
    {
        return count + "x " + ingredient;
    }
}
