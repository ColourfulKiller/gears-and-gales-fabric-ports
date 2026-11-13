package com.minecolonies.core.client.render.projectile;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.model.SpearModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.state.ThrownTridentRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;

import java.util.List;

/**
 * Renders a thrown spear (Fabric 1.21.11: follows vanilla's thrown trident renderer with the spear model).
 */
@Environment(EnvType.CLIENT)
public class RendererSpear extends EntityRenderer<ThrownTrident, ThrownTridentRenderState>
{
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/entity/spear.png");
    private final SpearModel model;

    public RendererSpear(final EntityRendererProvider.Context context)
    {
        super(context);
        this.model = new SpearModel(context.bakeLayer(ModelLayers.TRIDENT));
    }

    @Override
    public void submit(final ThrownTridentRenderState state, final PoseStack stack, final SubmitNodeCollector collector, final CameraRenderState camera)
    {
        stack.pushPose();
        stack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
        stack.mulPose(Axis.ZP.rotationDegrees(state.xRot + 90.0F));
        final List<RenderType> types = ItemRenderer.getFoilRenderTypes(this.model.renderType(TEXTURE), false, state.isFoil);
        for (int i = 0; i < types.size(); i++)
        {
            collector.order(i).submitModel(this.model, Unit.INSTANCE, stack, types.get(i), state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
        }
        stack.popPose();
        super.submit(state, stack, collector, camera);
    }

    @Override
    public ThrownTridentRenderState createRenderState()
    {
        return new ThrownTridentRenderState();
    }

    @Override
    public void extractRenderState(final ThrownTrident entity, final ThrownTridentRenderState state, final float partialTick)
    {
        super.extractRenderState(entity, state, partialTick);
        state.yRot = entity.getYRot(partialTick);
        state.xRot = entity.getXRot(partialTick);
        state.isFoil = entity.isFoil();
    }
}