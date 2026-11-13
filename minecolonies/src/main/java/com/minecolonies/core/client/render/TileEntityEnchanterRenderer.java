package com.minecolonies.core.client.render;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.minecolonies.core.tileentities.TileEntityEnchanter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Floating enchanting book above the enchanter hut (Fabric 1.21.11: render-state based).
 */
@Environment(EnvType.CLIENT)
public class TileEntityEnchanterRenderer implements BlockEntityRenderer<TileEntityColonyBuilding, TileEntityEnchanterRenderer.State>
{
    public static final Material TEXTURE_BOOK = new Material(TextureAtlas.LOCATION_BLOCKS, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "block/enchanting_table_book"));

    private final BookModel   modelBook;
    private final MaterialSet materials;

    public static class State extends BlockEntityRenderState
    {
        public boolean enchanter;
        public float   tick;
        public float   rotation;
        public float   pageFlip;
        public float   bookSpread;
    }

    public TileEntityEnchanterRenderer(final BlockEntityRendererProvider.Context context)
    {
        this.modelBook = new BookModel(context.bakeLayer(ModelLayers.BOOK));
        this.materials = context.materials();
    }

    @Override
    public State createRenderState()
    {
        return new State();
    }

    @Override
    public void extractRenderState(final TileEntityColonyBuilding ent, final State state, final float partialTicks, final Vec3 camera, @Nullable final ModelFeatureRenderer.CrumblingOverlay crumbling)
    {
        BlockEntityRenderer.super.extractRenderState(ent, state, partialTicks, camera, crumbling);
        state.enchanter = ent instanceof TileEntityEnchanter;
        if (ent instanceof final TileEntityEnchanter entity)
        {
            state.tick = (float) entity.tickCount + partialTicks;
            double rotVPrev = entity.bookRotation - entity.bookRotationPrev;
            float circleRot = (float) ((rotVPrev + Math.PI % (2 * Math.PI)) - Math.PI);
            state.rotation = entity.bookRotationPrev + circleRot * partialTicks;
            state.pageFlip = Mth.lerp(partialTicks, entity.pageFlipPrev, entity.pageFlip);
            state.bookSpread = Mth.lerp(partialTicks, entity.bookSpreadPrev, entity.bookSpread);
        }
    }

    @Override
    public void submit(final State state, final PoseStack matrixStack, final SubmitNodeCollector collector, final CameraRenderState camera)
    {
        if (!state.enchanter)
        {
            return;
        }
        matrixStack.pushPose();
        matrixStack.translate(0.5D, 0.75D, 0.5D);
        matrixStack.translate(0.0D, (0.1F + Mth.sin(state.tick * 0.1F) * 0.01F), 0.0D);
        matrixStack.mulPose(Axis.YP.rotation(-state.rotation));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
        float flipA = Mth.frac(state.pageFlip + 0.25F) * 1.6F - 0.3F;
        float flipB = Mth.frac(state.pageFlip + 0.75F) * 1.6F - 0.3F;
        final BookModel.State book = new BookModel.State(state.tick, Mth.clamp(flipA, 0.0F, 1.0F), Mth.clamp(flipB, 0.0F, 1.0F), state.bookSpread);
        collector.submitModel(this.modelBook, book, matrixStack, TEXTURE_BOOK.renderType(RenderTypes::entitySolid), state.lightCoords, OverlayTexture.NO_OVERLAY, -1,
          this.materials.get(TEXTURE_BOOK), 0, state.breakProgress);
        matrixStack.popPose();
    }
}