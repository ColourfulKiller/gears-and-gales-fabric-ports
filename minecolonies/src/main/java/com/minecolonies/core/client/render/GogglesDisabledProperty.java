package com.minecolonies.core.client.render;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.render.worldevent.ColonyBlueprintRenderer;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Item model condition "minecolonies:goggles_disabled": true while the build goggles preview is toggled off
 * (was the "disabled" item property on 1.21.1).
 */
@Environment(EnvType.CLIENT)
public record GogglesDisabledProperty() implements ConditionalItemModelProperty
{
    public static final MapCodec<GogglesDisabledProperty> MAP_CODEC = MapCodec.unit(new GogglesDisabledProperty());

    public static void register()
    {
        ConditionalItemModelProperties.ID_MAPPER.put(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "goggles_disabled"), MAP_CODEC);
    }

    @Override
    public boolean get(final ItemStack stack, @Nullable final ClientLevel level, @Nullable final LivingEntity entity, final int seed, final ItemDisplayContext context)
    {
        return ColonyBlueprintRenderer.isGogglesDisabled();
    }

    @Override
    public MapCodec<GogglesDisabledProperty> type()
    {
        return MAP_CODEC;
    }
}