package com.minecolonies.core.client.render;

import com.minecolonies.api.blocks.huts.AbstractBlockMinecoloniesDefault;
import com.minecolonies.api.tileentities.AbstractTileEntityScarecrow;
import com.minecolonies.api.tileentities.ScareCrowType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.blocks.BlockScarecrow;
import com.minecolonies.core.client.model.ScarecrowModel;
import com.minecolonies.core.event.ClientRegistryHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class to render the scarecrow (Fabric 1.21.11: render-state based).
 */
@Environment(EnvType.CLIENT)
public class TileEntityScarecrowRenderer implements BlockEntityRenderer<AbstractTileEntityScarecrow, TileEntityScarecrowRenderer.State>
{
    private static final double BLOCK_MIDDLE   = 0.5;
    private static final double YOFFSET        = 1.5;
    private static final int    ROTATION       = 180;
    private static final int    BASIC_ROTATION = 90;
    private static final int    ROTATE_EAST    = 1;
    private static final int    ROTATE_SOUTH   = 2;
    private static final int    ROTATE_WEST    = 3;

    public static final Material SCARECROW_A = new Material(TextureAtlas.LOCATION_BLOCKS, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "block/blockscarecrowpumpkin"));
    public static final Material SCARECROW_B = new Material(TextureAtlas.LOCATION_BLOCKS, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "block/blockscarecrownormal"));

    @NotNull
    private final ScarecrowModel model;
    private final MaterialSet    materials;

    public static class State extends BlockEntityRenderState
    {
        public boolean   render;
        public boolean   lantern;
        public boolean   pumpkin;
        public @Nullable Direction facing;
    }

    public TileEntityScarecrowRenderer(final BlockEntityRendererProvider.Context context)
    {
        this.model = new ScarecrowModel(context.bakeLayer(ClientRegistryHandler.SCARECROW));
        this.materials = context.materials();
    }

    @Override
    public State createRenderState()
    {
        return new State();
    }

    @Override
    public void extractRenderState(final AbstractTileEntityScarecrow te, final State state, final float partialTicks, final Vec3 camera, @Nullable final ModelFeatureRenderer.CrumblingOverlay crumbling)
    {
        BlockEntityRenderer.super.extractRenderState(te, state, partialTicks, camera, crumbling);
        state.render = state.blockState.getBlock() instanceof BlockScarecrow && state.blockState.getValue(BlockScarecrow.HALF) != DoubleBlockHalf.UPPER;
        state.lantern = state.render && state.blockState.getValue(BlockScarecrow.LANTERN);
        state.facing = state.render ? state.blockState.getValue(AbstractBlockMinecoloniesDefault.FACING) : null;
        state.pumpkin = te.getScarecrowType() == ScareCrowType.PUMPKINHEAD;
    }

    @Override
    public void submit(final State state, final PoseStack matrixStack, final SubmitNodeCollector collector, final CameraRenderState camera)
    {
        if (!state.render)
        {
            return;
        }

        matrixStack.pushPose();
        matrixStack.translate(BLOCK_MIDDLE, YOFFSET, BLOCK_MIDDLE);
        matrixStack.mulPose(Axis.ZP.rotationDegrees(ROTATION));
        if (state.facing != null)
        {
            switch (state.facing)
            {
                case EAST -> matrixStack.mulPose(Axis.YP.rotationDegrees(BASIC_ROTATION * ROTATE_EAST));
                case SOUTH -> matrixStack.mulPose(Axis.YP.rotationDegrees(BASIC_ROTATION * ROTATE_SOUTH));
                case WEST -> matrixStack.mulPose(Axis.YP.rotationDegrees(BASIC_ROTATION * ROTATE_WEST));
                default ->
                {
                }
            }
        }

        final Material material = state.pumpkin ? SCARECROW_A : SCARECROW_B;
        collector.submitModel(this.model, Unit.INSTANCE, matrixStack, material.renderType(RenderTypes::entitySolid), state.lightCoords, OverlayTexture.NO_OVERLAY, -1,
          this.materials.get(material), 0, state.breakProgress);

        if (state.lantern)
        {
            matrixStack.pushPose();
            matrixStack.mulPose(Axis.ZP.rotationDegrees(180f));
            matrixStack.translate(0.6f, -0.6f, -0.375f);
            matrixStack.scale(0.75f, 0.65f, 0.75f);
            collector.submitBlock(matrixStack, Blocks.LANTERN.defaultBlockState(), state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            matrixStack.popPose();
        }
        matrixStack.popPose();
    }
}