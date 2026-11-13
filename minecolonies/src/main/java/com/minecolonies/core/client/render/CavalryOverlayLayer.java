package com.minecolonies.core.client.render;

import com.minecolonies.api.util.constant.Constants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/**
 * Combat-readiness overlay on cavalry horses. Fabric 1.21.11 port: the readiness segment is extracted into the horse's
 * render state by {@link CavalryHorseRenderer} (Fabric render-state data) and drawn from there.
 */
public class CavalryOverlayLayer extends RenderLayer<HorseRenderState, HorseModel>
{
    /** Readiness segment 0..5, or absent for horses that are not cavalry. */
    public static final RenderStateDataKey<Integer> SEGMENTS = RenderStateDataKey.create(() -> "minecolonies:cavalry_readiness");

    public CavalryOverlayLayer(final RenderLayerParent<HorseRenderState, HorseModel> parent)
    {
        super(parent);
    }

    @Override
    public void submit(final PoseStack pose, final SubmitNodeCollector collector, final int packedLight, final HorseRenderState state, final float yRot, final float xRot)
    {
        final Integer segments = state.getData(SEGMENTS);
        if (segments == null)
        {
            return;
        }
        final Identifier texture = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/entity/horse/cavalry_overlay_layer" + segments + ".png");
        // 0.85f alpha -> 217 (out of 255)
        final int color = ARGB.color((int) (0.85f * 255.0f), 255, 255, 255);
        collector.submitModel(this.getParentModel(), state, pose, RenderTypes.entityTranslucent(texture), packedLight, OverlayTexture.NO_OVERLAY, color, null, state.outlineColor, null);
    }
}