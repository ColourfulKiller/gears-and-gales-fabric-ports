package com.minecolonies.core.client.render;

import com.google.common.collect.Maps;
import com.minecolonies.core.entity.other.cavalry.CavalryHorseEntity;
import net.minecraft.client.model.animal.equine.EquineSaddleModel;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.HorseMarkingLayer;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Variant;

import java.util.Map;

/**
 * Renderer for cavalry horses: vanilla's horse renderer (which is final in 1.21.11, so it is mirrored here) plus the
 * combat-readiness overlay.
 */
public class CavalryHorseRenderer extends AbstractHorseRenderer<Horse, HorseRenderState, HorseModel>
{
    private static final Map<Variant, Identifier> LOCATION_BY_VARIANT = Maps.newEnumMap(Map.of(
        Variant.WHITE, Identifier.withDefaultNamespace("textures/entity/horse/horse_white.png"),
        Variant.CREAMY, Identifier.withDefaultNamespace("textures/entity/horse/horse_creamy.png"),
        Variant.CHESTNUT, Identifier.withDefaultNamespace("textures/entity/horse/horse_chestnut.png"),
        Variant.BROWN, Identifier.withDefaultNamespace("textures/entity/horse/horse_brown.png"),
        Variant.BLACK, Identifier.withDefaultNamespace("textures/entity/horse/horse_black.png"),
        Variant.GRAY, Identifier.withDefaultNamespace("textures/entity/horse/horse_gray.png"),
        Variant.DARK_BROWN, Identifier.withDefaultNamespace("textures/entity/horse/horse_darkbrown.png")));

    public CavalryHorseRenderer(final EntityRendererProvider.Context context)
    {
        super(context, new HorseModel(context.bakeLayer(ModelLayers.HORSE)), new HorseModel(context.bakeLayer(ModelLayers.HORSE_BABY)));
        this.addLayer(new HorseMarkingLayer(this));
        this.addLayer(new SimpleEquipmentLayer<>(this, context.getEquipmentRenderer(), EquipmentClientInfo.LayerType.HORSE_BODY, state -> state.bodyArmorItem,
            new HorseModel(context.bakeLayer(ModelLayers.HORSE_ARMOR)), new HorseModel(context.bakeLayer(ModelLayers.HORSE_BABY_ARMOR)), 2));
        this.addLayer(new SimpleEquipmentLayer<>(this, context.getEquipmentRenderer(), EquipmentClientInfo.LayerType.HORSE_SADDLE, state -> state.saddle,
            new EquineSaddleModel(context.bakeLayer(ModelLayers.HORSE_SADDLE)), new EquineSaddleModel(context.bakeLayer(ModelLayers.HORSE_BABY_SADDLE)), 2));
        this.addLayer(new CavalryOverlayLayer(this));
    }

    @Override
    public Identifier getTextureLocation(final HorseRenderState state)
    {
        return LOCATION_BY_VARIANT.get(state.variant);
    }

    @Override
    public HorseRenderState createRenderState()
    {
        return new HorseRenderState();
    }

    @Override
    public void extractRenderState(final Horse horse, final HorseRenderState state, final float partialTick)
    {
        super.extractRenderState(horse, state, partialTick);
        state.variant = horse.getVariant();
        state.markings = horse.getMarkings();
        state.bodyArmorItem = horse.getBodyArmorItem().copy();

        if (horse instanceof final CavalryHorseEntity cavhorse)
        {
            final float threshold = horse.getMaxHealth() * CavalryHorseEntity.COMBAT_READINESS_THRESHOLD;
            final float cooldown = Math.max(0f, cavhorse.getAnimalDataView() == null ? 0 : cavhorse.getAnimalDataView().getCombatCooldown());
            final float readiness = Mth.clamp(1.0f - (cooldown / Math.max(0.001f, threshold)), 0f, 1f);
            state.setData(CavalryOverlayLayer.SEGMENTS, Mth.clamp((int) Math.floor(readiness * 5f + 0.0001f), 0, 5));
        }
        else
        {
            state.setData(CavalryOverlayLayer.SEGMENTS, null);
        }
    }
}