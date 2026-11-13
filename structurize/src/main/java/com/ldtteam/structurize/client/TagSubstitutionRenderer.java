package com.ldtteam.structurize.client;

import com.ldtteam.common.fakelevel.SingleBlockFakeLevel;
import com.ldtteam.structurize.blockentities.BlockEntityTagSubstitution;
import com.ldtteam.structurize.component.CapturedBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * Renders the replacement block inside a tag substitution block (and its item).
 * <p>
 * Fabric 1.21.11 port: 1.21.1 was a BlockEntityRenderer + BlockEntityWithoutLevelRenderer (item) that drew directly;
 * now it's a render-state block entity renderer plus a {@link SpecialModelRenderer} for the item (item definition
 * {@code structurize:tag_substitution}, composited over the plain overlay model). The block's own overlay model is a
 * normal block model now (the NeoForge "overlaid" loader only made it a custom renderer).
 */
public class TagSubstitutionRenderer implements BlockEntityRenderer<BlockEntityTagSubstitution, TagSubstitutionRenderer.State>
{
    private static final float SCALE = 0.995f;
    private static final float SHIFT = 0.0025f;

    private SingleBlockFakeLevel renderLevel;

    public TagSubstitutionRenderer(@NotNull final BlockEntityRendererProvider.Context context)
    {
    }

    public static class State extends BlockEntityRenderState
    {
        BlockState replacement;
        @Nullable BlockEntityRenderState nested;
    }

    @Override
    public State createRenderState()
    {
        return new State();
    }

    @Override
    public void extractRenderState(final BlockEntityTagSubstitution entity,
        final State state,
        final float partialTick,
        final Vec3 cameraPos,
        @Nullable final ModelFeatureRenderer.CrumblingOverlay crumbling)
    {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTick, cameraPos, crumbling);
        final CapturedBlock replacement = entity.getReplacement();
        state.replacement = replacement.blockState();
        state.nested = null;

        final Level realLevel = entity.getLevel();
        if (replacement.blockState().isAir() || !replacement.hasBlockEntity() || realLevel == null)
        {
            return;
        }
        final BlockEntity nestedBe = BlockEntity.loadStatic(BlockPos.ZERO, replacement.blockState(), replacement.serializedBE().get(), realLevel.registryAccess());
        if (nestedBe == null)
        {
            return;
        }
        final BlockEntityRenderer<BlockEntity, BlockEntityRenderState> nestedRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(nestedBe);
        if (nestedRenderer == null)
        {
            return;
        }
        if (renderLevel == null)
        {
            renderLevel = new SingleBlockFakeLevel(realLevel);
        }
        renderLevel.withFakeLevelContext(replacement.blockState(), nestedBe, realLevel, fakeLevel -> {
            final BlockEntityRenderState nested = nestedRenderer.createRenderState();
            nestedRenderer.extractRenderState(nestedBe, nested, partialTick, cameraPos.subtract(Vec3.atLowerCornerOf(entity.getBlockPos())), null);
            nested.lightCoords = state.lightCoords;
            state.nested = nested;
        });
    }

    @Override
    public void submit(final State state, final PoseStack poseStack, final SubmitNodeCollector collector, final CameraRenderState camera)
    {
        if (state.replacement == null || state.replacement.isAir())
        {
            return;
        }
        poseStack.pushPose();
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.translate(SHIFT, SHIFT, SHIFT);
        collector.submitBlock(poseStack, state.replacement, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        if (state.nested != null)
        {
            Minecraft.getInstance().getBlockEntityRenderDispatcher().submit(state.nested, poseStack, collector, camera);
        }
        poseStack.popPose();
    }

    /**
     * Item: the replacement block, drawn over the anchor's overlay model by the composite item definition.
     */
    public static class ItemRenderer implements SpecialModelRenderer<BlockState>
    {
        @Override
        public void submit(@Nullable final BlockState replacement,
            final ItemDisplayContext displayContext,
            final PoseStack poseStack,
            final SubmitNodeCollector collector,
            final int light,
            final int overlay,
            final boolean glint,
            final int outlineColor)
        {
            if (replacement == null || replacement.isAir())
            {
                return;
            }
            poseStack.pushPose();
            poseStack.scale(SCALE, SCALE, SCALE);
            poseStack.translate(SHIFT, SHIFT, SHIFT);
            collector.submitBlock(poseStack, replacement, light, overlay, outlineColor);
            poseStack.popPose();
        }

        @Override
        public void getExtents(final Consumer<Vector3fc> consumer)
        {
            consumer.accept(new org.joml.Vector3f(0, 0, 0));
            consumer.accept(new org.joml.Vector3f(1, 1, 1));
        }

        @Override
        @Nullable
        public BlockState extractArgument(final ItemStack stack)
        {
            final BlockState state = CapturedBlock.readFromItemStack(stack).blockState();
            return state.isAir() ? null : state;
        }

        public record Unbaked() implements SpecialModelRenderer.Unbaked
        {
            public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

            @Override
            public SpecialModelRenderer<?> bake(final SpecialModelRenderer.BakingContext context)
            {
                return new ItemRenderer();
            }

            @Override
            public MapCodec<Unbaked> type()
            {
                return MAP_CODEC;
            }
        }
    }
}
