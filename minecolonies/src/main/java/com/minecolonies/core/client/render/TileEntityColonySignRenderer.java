package com.minecolonies.core.client.render;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.core.client.render.worldevent.WorldEventContext;
import com.minecolonies.core.tileentities.TileEntityColonySign;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Style;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;

import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.NEXT;
import static com.minecolonies.api.util.constant.TranslationConstants.PREVIOUS;
import static com.minecolonies.core.blocks.BlockColonySign.CONNECTED;

@Environment(EnvType.CLIENT)
public class TileEntityColonySignRenderer implements BlockEntityRenderer<TileEntityColonySign, TileEntityColonySignRenderer.State>
{
    private static final int MAX_SIZE = 20;

    /** One text line on the sign face. */
    public record Line(FormattedCharSequence text, float x, float y) {}

    public static class State extends BlockEntityRenderState
    {
        public boolean    render;
        public float      rotation;
        public boolean    connected;
        public final List<Line> lines = new ArrayList<>();
    }

    public TileEntityColonySignRenderer(final BlockEntityRendererProvider.Context context)
    {
        super();
    }

    @Override
    public State createRenderState()
    {
        return new State();
    }

    @Override
    public void extractRenderState(final TileEntityColonySign sign, final State state, final float partialTicks, final Vec3 camera, @Nullable final ModelFeatureRenderer.CrumblingOverlay crumbling)
    {
        BlockEntityRenderer.super.extractRenderState(sign, state, partialTicks, camera, crumbling);
        state.render = state.blockState.getBlock() == ModBlocks.blockColonySign;
        state.rotation = sign.getRelativeRotation();
        state.connected = sign.getTargetColonyId() != sign.getCachedSignAboveColony();
        state.lines.clear();
        if (!state.render)
        {
            return;
        }

        final String colonyName = sign.getColonyName();
        final int distance = sign.getColonyDistance();
        if (colonyName.isEmpty())
        {
            addLine(state, "Unknown Colony", 0, 0);
            addLine(state, Component.translatable("com.minecolonies.coremod.dist.blocks", distance).getString(), 3, 0);
        }
        else
        {
            final String targetColonyName = sign.getTargetColonyName();
            if (!targetColonyName.isEmpty() && state.connected)
            {
                addColonyName(state, colonyName, distance, -10);
                addColonyName(state, targetColonyName, sign.getTargetColonyDistance(), -60);
            }
            else
            {
                addColonyName(state, colonyName, distance, -35);
            }
        }
    }

    private static void addColonyName(final State state, final String colonyName, final int distance, final int offset)
    {
        final Font font = Minecraft.getInstance().font;
        if (font.width(colonyName) > 90)
        {
            final List<FormattedText> splitName = font.getSplitter().splitLines(colonyName, 90, Style.EMPTY);
            for (int i = 0; i < Math.min(2, splitName.size()); i++)
            {
                addLine(state, splitName.get(i).getString(), i, offset);
            }
        }
        else
        {
            addLine(state, colonyName, 0, offset);
        }
        addLine(state, Component.translatable("com.minecolonies.coremod.dist.blocks", distance).getString(), 3, offset);
    }

    private static void addLine(final State state, String text, final int line, final float offset)
    {
        if (text.length() > MAX_SIZE)
        {
            text = text.substring(0, MAX_SIZE);
        }
        final FormattedCharSequence sequence = FormattedCharSequence.forward(text, Style.EMPTY);
        final float x = (float) (-Minecraft.getInstance().font.width(sequence) / 2);
        state.lines.add(new Line(sequence, x, line * 8f + offset));
    }

    @Override
    public void submit(final State state, final PoseStack matrixStack, final SubmitNodeCollector collector, final CameraRenderState camera)
    {
        if (!state.render)
        {
            return;
        }

        matrixStack.pushPose();
        matrixStack.translate(0.5, 0.5, 0.5);
        matrixStack.mulPose(Axis.YP.rotationDegrees(state.rotation));
        matrixStack.translate(-0.5, -0.5, -0.5);
        collector.submitBlock(matrixStack, state.blockState.setValue(CONNECTED, state.connected), state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        matrixStack.popPose();

        submitTextOnSide(state, matrixStack, collector, true);
        submitTextOnSide(state, matrixStack, collector, false);
    }

    private static void submitTextOnSide(final State state, final PoseStack matrixStack, final SubmitNodeCollector collector, final boolean mirrored)
    {
        matrixStack.pushPose();
        matrixStack.translate(0.5f, 0.5F, 0.5f);
        matrixStack.mulPose(Axis.YP.rotationDegrees(state.rotation));
        if (mirrored)
        {
            matrixStack.mulPose(Axis.YP.rotationDegrees(180));
        }
        matrixStack.translate(-0.0f, -0.1F, 0.2f);
        matrixStack.scale(0.007F, -0.007F, 0.007F);
        for (final Line line : state.lines)
        {
            collector.submitText(matrixStack, line.x(), line.y(), line.text(), false, Font.DisplayMode.NORMAL, state.lightCoords, 0xdcdcdc00, 0, 0);
        }
        matrixStack.popPose();
    }

    public static void renderSignHover(final WorldEventContext context)
    {
        final HitResult rayTraceResult = Minecraft.getInstance().hitResult;
        if (!(rayTraceResult instanceof final BlockHitResult blockRayTraceResult) || blockRayTraceResult.getType() == HitResult.Type.MISS)
            return;

        final BlockPos posAtCamera = blockRayTraceResult.getBlockPos();
        if (context.clientLevel.getBlockState(posAtCamera).getBlock() != ModBlocks.blockColonySign)
        {
            return;
        }

        if (context.clientLevel.getBlockEntity(posAtCamera) instanceof TileEntityColonySign tileEntityColonySign)
        {
            if (!BlockPos.ZERO.equals(tileEntityColonySign.getPreviousPos()))
            {
                renderTextBoxAtPos(context, tileEntityColonySign.getPreviousPos(), List.of(Component.translatable(PREVIOUS).getString()));
            }
            if (!BlockPos.ZERO.equals(tileEntityColonySign.getNextPosition()))
            {
                renderTextBoxAtPos(context, tileEntityColonySign.getNextPosition(), List.of(Component.translatable(NEXT).getString()));
            }
        }
    }

    private static void renderTextBoxAtPos(final WorldEventContext context, final BlockPos pos, final List<String> text)
    {
        context.pushPoseCameraToPos(pos);
        context.renderLineBoxWithShadow(BlockPos.ZERO, 0xffffffff, WorldEventContext.DEFAULT_LINE_WIDTH * 2);
        context.popPose();
        renderDebugText(pos, text, context.poseStack, true, 3, 2.5f, context.bufferSource, context.cameraPosition);
    }

    public static void renderDebugText(final BlockPos renderPos,
        final List<String> text,
        final PoseStack matrixStack,
        final boolean forceWhite,
        final int mergeEveryXListElements,
        final float scale,
        final MultiBufferSource buffer, final Vec3 cameraPosition)
    {
        if (mergeEveryXListElements < 1)
        {
            throw new IllegalArgumentException("mergeEveryXListElements is less than 1");
        }

        final EntityRenderDispatcher erm = Minecraft.getInstance().getEntityRenderDispatcher();
        final int cap = text.size();
        if (cap > 0)
        {
            final Font fontrenderer = Minecraft.getInstance().font;

            matrixStack.pushPose();
            matrixStack.translate(renderPos.getX() + 0.5d - cameraPosition.x(), renderPos.getY() + 0.6d - cameraPosition.y(), renderPos.getZ() + 0.5d - cameraPosition.z());
            matrixStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
            matrixStack.scale(0.014f, -0.014f, 0.014f);

            final float backgroundTextOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
            final int alphaMask = (int) (backgroundTextOpacity * 255.0F) << 24;

            final Matrix4f rawPosMatrix = matrixStack.last().pose();
            rawPosMatrix.scale(scale, scale, scale);

            for (int i = 0; i < cap; i += mergeEveryXListElements)
            {
                final MutableComponent renderText = Component.literal(
                    mergeEveryXListElements == 1 ? text.get(i) : text.subList(i, Math.min(i + mergeEveryXListElements, cap)).toString());
                final float textCenterShift = (float) (-fontrenderer.width(renderText) / 2);

                fontrenderer.drawInBatch(renderText,
                    textCenterShift,
                    0,
                    forceWhite ? 0xffffffff : 0x20ffffff,
                    false,
                    rawPosMatrix,
                    buffer,
                    Font.DisplayMode.SEE_THROUGH,
                    alphaMask,
                    0x00f000f0);
                if (!forceWhite)
                {
                    fontrenderer.drawInBatch(renderText, textCenterShift, 0, 0xffffffff, false, rawPosMatrix, buffer, Font.DisplayMode.NORMAL, 0, 0x00f000f0);
                }
                matrixStack.translate(0.0d, fontrenderer.lineHeight + 1, 0.0d);
            }

            matrixStack.popPose();
        }
    }
}
