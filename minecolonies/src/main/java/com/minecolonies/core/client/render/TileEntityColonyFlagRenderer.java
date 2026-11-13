package com.minecolonies.core.client.render;

import com.minecolonies.core.blocks.decorative.BlockColonyFlagBanner;
import com.minecolonies.core.blocks.decorative.BlockColonyFlagWallBanner;
import com.minecolonies.core.tileentities.TileEntityColonyFlag;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BannerRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Renders the colony flag as a white banner carrying the colony's patterns.
 * Fabric 1.21.11 port: delegates the drawing to vanilla's BannerRenderer with a filled-in banner render state.
 */
@Environment(EnvType.CLIENT)
public class TileEntityColonyFlagRenderer implements BlockEntityRenderer<TileEntityColonyFlag, BannerRenderState>
{
    private final BannerRenderer banner;

    public TileEntityColonyFlagRenderer(final BlockEntityRendererProvider.Context context)
    {
        this.banner = new BannerRenderer(context);
    }

    @Override
    public BannerRenderState createRenderState()
    {
        return new BannerRenderState();
    }

    @Override
    public void extractRenderState(final TileEntityColonyFlag flag, final BannerRenderState state, final float partialTicks, final Vec3 camera, @Nullable final ModelFeatureRenderer.CrumblingOverlay crumbling)
    {
        BlockEntityRenderer.super.extractRenderState(flag, state, partialTicks, camera, crumbling);
        state.baseColor = DyeColor.WHITE;
        state.patterns = flag.getPatterns();
        final BlockState blockState = flag.getBlockState();
        if (blockState.getBlock() instanceof BlockColonyFlagWallBanner)
        {
            state.angle = -blockState.getValue(BlockColonyFlagWallBanner.HORIZONTAL_FACING).toYRot();
            state.standing = false;
        }
        else
        {
            state.angle = blockState.getBlock() instanceof BlockColonyFlagBanner ? -(blockState.getValue(BlockColonyFlagBanner.ROTATION) * 360) / 16.0F : 0;
            state.standing = true;
        }
        final long time = flag.getLevel() != null ? flag.getLevel().getGameTime() : 0L;
        final BlockPos pos = flag.getBlockPos();
        state.phase = ((float) Math.floorMod(pos.getX() * 7 + pos.getY() * 9 + pos.getZ() * 13 + time, 100L) + partialTicks) / 100.0F;
    }

    @Override
    public void submit(final BannerRenderState state, final PoseStack poseStack, final SubmitNodeCollector collector, final CameraRenderState camera)
    {
        banner.submit(state, poseStack, collector, camera);
    }
}