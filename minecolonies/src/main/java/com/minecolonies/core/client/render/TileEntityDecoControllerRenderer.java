package com.minecolonies.core.client.render;

import com.minecolonies.core.blocks.BlockDecorationController;
import com.minecolonies.core.tileentities.TileEntityDecorationController;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Renders the decoration controller flush against the block it is attached to (Fabric 1.21.11: render-state based).
 */
@Environment(EnvType.CLIENT)
public class TileEntityDecoControllerRenderer implements BlockEntityRenderer<BlockEntity, TileEntityDecoControllerRenderer.State>
{
    public static class State extends BlockEntityRenderState
    {
        public boolean render;
        public Vec3    offset = Vec3.ZERO;
    }

    public TileEntityDecoControllerRenderer(final BlockEntityRendererProvider.Context context)
    {
    }

    @Override
    public State createRenderState()
    {
        return new State();
    }

    @Override
    public void extractRenderState(final BlockEntity blockEntity, final State state, final float partialTick, final Vec3 camera, @Nullable final ModelFeatureRenderer.CrumblingOverlay crumbling)
    {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, camera, crumbling);
        state.render = false;
        final Level level = blockEntity.getLevel();
        if (!(blockEntity instanceof TileEntityDecorationController) || level == null || state.blockState.isAir())
        {
            return;
        }
        final Direction direction = state.blockState.getValue(BlockDecorationController.FACING);
        final BlockPos offsetPos = blockEntity.getBlockPos().relative(direction);
        final BlockState neighbour = level.getBlockState(offsetPos);
        final VoxelShape shape = neighbour.getShape(level, offsetPos);
        state.render = true;
        if (shape.isEmpty() || Block.isShapeFullBlock(shape))
        {
            state.offset = Vec3.ZERO;
            return;
        }
        state.offset = switch (direction)
        {
            case UP -> new Vec3(0, shape.min(Direction.Axis.Y), 0);
            case DOWN -> new Vec3(0, shape.max(Direction.Axis.Y) - 1, 0);
            case NORTH -> new Vec3(0, 0, shape.max(Direction.Axis.Z) - 1);
            case SOUTH -> new Vec3(0, 0, shape.min(Direction.Axis.Z));
            case EAST -> new Vec3(shape.min(Direction.Axis.X), 0, 0);
            case WEST -> new Vec3(shape.max(Direction.Axis.X) - 1, 0, 0);
        };
    }

    @Override
    public void submit(final State state, final PoseStack matrixStack, final SubmitNodeCollector collector, final CameraRenderState camera)
    {
        if (!state.render)
        {
            return;
        }
        matrixStack.pushPose();
        matrixStack.translate(state.offset.x, state.offset.y, state.offset.z);
        collector.submitBlock(matrixStack, state.blockState, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        matrixStack.popPose();
    }
}