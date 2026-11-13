package com.minecolonies.core.client.render;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.blocks.huts.AbstractBlockMinecoloniesDefault;
import com.minecolonies.core.tileentities.TileEntityNamedGrave;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the citizen's name on a named grave (Fabric 1.21.11: render-state based).
 */
@Environment(EnvType.CLIENT)
public class TileEntityNamedGraveRenderer implements BlockEntityRenderer<TileEntityNamedGrave, TileEntityNamedGraveRenderer.State>
{
    private static final int BASIC_ROTATION = 90;
    private static final int ROTATE_EAST    = 1;
    private static final int ROTATE_NORTH   = 2;
    private static final int ROTATE_WEST    = 3;
    private static final int MAX_SIZE       = 20;

    public static class State extends BlockEntityRenderState
    {
        public @Nullable Direction facing;
        public final List<String> lines = new ArrayList<>();
    }

    public TileEntityNamedGraveRenderer(final BlockEntityRendererProvider.Context context)
    {
        super();
    }

    @Override
    public State createRenderState()
    {
        return new State();
    }

    @Override
    public void extractRenderState(final TileEntityNamedGrave grave, final State state, final float partialTicks, final Vec3 camera, @Nullable final ModelFeatureRenderer.CrumblingOverlay crumbling)
    {
        BlockEntityRenderer.super.extractRenderState(grave, state, partialTicks, camera, crumbling);
        state.facing = state.blockState.getBlock() == ModBlocks.blockNamedGrave ? state.blockState.getValue(AbstractBlockMinecoloniesDefault.FACING) : null;
        state.lines.clear();
        if (grave.getTextLines().isEmpty())
        {
            state.lines.add("Unknown Citizen");
        }
        else
        {
            state.lines.addAll(grave.getTextLines());
        }
    }

    @Override
    public void submit(final State state, final PoseStack matrixStack, final SubmitNodeCollector collector, final CameraRenderState camera)
    {
        if (state.facing == null)
        {
            return;
        }
        matrixStack.pushPose();
        switch (state.facing)
        {
            case NORTH ->
            {
                matrixStack.translate(0.5f, 1.18F, 0.48F);
                matrixStack.scale(0.006F, -0.006F, 0.006F);
                matrixStack.mulPose(Axis.YP.rotationDegrees(BASIC_ROTATION * ROTATE_NORTH));
            }
            case SOUTH ->
            {
                matrixStack.translate(0.5f, 1.18F, 0.54F);
                matrixStack.scale(0.006F, -0.006F, 0.006F);
            }
            case EAST ->
            {
                matrixStack.translate(0.54f, 1.18F, 0.5F);
                matrixStack.scale(0.006F, -0.006F, 0.006F);
                matrixStack.mulPose(Axis.YP.rotationDegrees(BASIC_ROTATION * ROTATE_EAST));
            }
            case WEST ->
            {
                matrixStack.translate(0.48f, 1.18F, 0.5F);
                matrixStack.scale(0.006F, -0.006F, 0.006F);
                matrixStack.mulPose(Axis.YP.rotationDegrees(BASIC_ROTATION * ROTATE_WEST));
            }
            default ->
            {
            }
        }

        final Font font = Minecraft.getInstance().font;
        for (int i = 0; i < state.lines.size(); i++)
        {
            String text = state.lines.get(i);
            if (text.length() > MAX_SIZE)
            {
                text = text.substring(0, MAX_SIZE);
            }
            final FormattedCharSequence sequence = FormattedCharSequence.forward(text, Style.EMPTY);
            final float x = (float) (-font.width(sequence) / 2);
            collector.submitText(matrixStack, x, i * 10f, sequence, false, Font.DisplayMode.NORMAL, state.lightCoords, 0xdcdcdc00, 0, 0);
        }
        matrixStack.popPose();
    }
}