package com.minecolonies.core.client.render;

import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;

/**
 * Renders nothing; registered so hut block entities have a renderer.
 */
@Environment(EnvType.CLIENT)
public class EmptyTileEntitySpecialRenderer implements BlockEntityRenderer<AbstractTileEntityColonyBuilding, BlockEntityRenderState>
{
    public EmptyTileEntitySpecialRenderer(final BlockEntityRendererProvider.Context context)
    {
        super();
    }

    @Override
    public BlockEntityRenderState createRenderState()
    {
        return new BlockEntityRenderState();
    }

    @Override
    public void submit(final BlockEntityRenderState state, final PoseStack poseStack, final SubmitNodeCollector collector, final CameraRenderState camera)
    {
    }
}