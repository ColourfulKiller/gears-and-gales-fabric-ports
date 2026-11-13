package com.ldtteam.fabric.crafting;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * NeoForge's IngredientType (a codec registered under an id) as a Fabric custom ingredient serializer. Create it, then
 * {@link #register(Identifier)} it once during mod init. {@link #get()} returns itself, so code written against
 * DeferredHolder<IngredientType> keeps working.
 */
public final class IngredientType<T extends ICustomIngredient>
{
    private final MapCodec<T> codec;
    private CustomIngredientSerializer<T> serializer;

    public IngredientType(final MapCodec<T> codec)
    {
        this.codec = codec;
    }

    public IngredientType<T> get()
    {
        return this;
    }

    public MapCodec<T> codec()
    {
        return codec;
    }

    public IngredientType<T> register(final Identifier id)
    {
        final StreamCodec<RegistryFriendlyByteBuf, T> packetCodec = ByteBufCodecs.fromCodecWithRegistries(codec.codec());
        serializer = new CustomIngredientSerializer<>()
        {
            @Override
            public Identifier getIdentifier()
            {
                return id;
            }

            @Override
            public MapCodec<T> getCodec()
            {
                return codec;
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, T> getPacketCodec()
            {
                return packetCodec;
            }
        };
        CustomIngredientSerializer.register(serializer);
        return this;
    }

    public CustomIngredientSerializer<T> serializer()
    {
        if (serializer == null)
        {
            throw new IllegalStateException("IngredientType used before it was registered");
        }
        return serializer;
    }
}