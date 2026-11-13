package com.ldtteam.structurize.client;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.blockentities.BlockEntityTagSubstitution;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtils;
import com.ldtteam.structurize.client.fakelevel.BlueprintBlockAccess;
import com.ldtteam.structurize.client.render.RenderLevelStage;
import com.ldtteam.structurize.component.CapturedBlock;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.ldtteam.structurize.tag.ModTags;
import com.ldtteam.structurize.util.BlockInfo;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.CrashReport;
import net.minecraft.ReportType;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;

/**
 * The renderer for blueprint.
 * Holds all information required to render a blueprint.
 * <p>
 * Fabric 1.21.11 port: the 1.21.1 version uploaded per-layer VertexBuffers and drew them with the chunk shaders and a
 * GL blend-colour hack. Now the blocks are meshed once (Fabric's block renderer, so emitQuads models such as Domum's
 * retexture correctly) into persistent GPU buffers and drawn with the vanilla "moving block" pipelines; the
 * DynamicTransforms uniform carries the blueprint offset (ModelOffset) and the preview transparency (ColorModulator).
 * Block entities and entities go through vanilla's render-state / submit queue at {@link RenderLevelStage.Stage#AFTER_SOLID_BLOCKS}.
 */
public class BlueprintRenderer implements AutoCloseable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BlueprintRenderer.class);

    /** Layers in draw order; tripwire is drawn after translucent like vanilla. */
    private static final ChunkSectionLayer[] LAYERS = {ChunkSectionLayer.SOLID, ChunkSectionLayer.CUTOUT, ChunkSectionLayer.TRANSLUCENT, ChunkSectionLayer.TRIPWIRE};

    /** Same as the vanilla moving-block pipelines, but blended so the preview can be see-through. */
    private static final RenderPipeline SOLID_BLENDED = RenderPipeline.builder(RenderPipelines.BLOCK_SNIPPET)
        .withLocation(com.ldtteam.structurize.api.constants.Constants.resLocStruct("pipeline/preview_solid"))
        .withBlend(BlendFunction.TRANSLUCENT)
        .build();
    private static final RenderPipeline CUTOUT_BLENDED = RenderPipeline.builder(RenderPipelines.BLOCK_SNIPPET)
        .withLocation(com.ldtteam.structurize.api.constants.Constants.resLocStruct("pipeline/preview_cutout"))
        .withShaderDefine("ALPHA_CUTOUT", 0.5F)
        .withBlend(BlendFunction.TRANSLUCENT)
        .build();

    private static boolean hasWarnedExceptions = false;

    private final BlueprintBlockAccess blockAccess;
    List<Entity> entities = List.of();
    private List<BlockEntity> tileEntities = List.of();
    private Map<ChunkSectionLayer, Mesh> meshes;
    private long lastGameTime;
    private boolean bypassMainFrustum = false;
    private Set<Object> crashingObjects = Collections.newSetFromMap(new IdentityHashMap<>());

    private record Mesh(GpuBuffer vertices, int indexCount, VertexFormat.Mode mode) implements AutoCloseable
    {
        @Override
        public void close()
        {
            vertices.close();
        }
    }

    /**
     * Static factory utility method to handle the extraction of the values from the blueprint.
     *
     * @param blueprint The blueprint to create an instance for.
     * @return The renderer.
     */
    public static BlueprintRenderer buildRendererForBlueprint(final Blueprint blueprint)
    {
        final BlueprintBlockAccess blockAccess = new BlueprintBlockAccess(blueprint);
        return new BlueprintRenderer(blockAccess);
    }

    private BlueprintRenderer(final BlueprintBlockAccess blockAccess)
    {
        this.blockAccess = blockAccess;
    }

    /**
     * Updates blueprint reference if it has same hash.
     *
     * @param previewData blueprint and context from active structure
     */
    public void updateBlueprint(final BlueprintPreviewData previewData)
    {
        if (blockAccess.getLevelSource() != previewData.getBlueprint() && blockAccess.getLevelSource().hashCode() == previewData.getBlueprint().hashCode())
        {
            blockAccess.setLevelSource(previewData.getBlueprint());
        }
    }

    private void init(final BlueprintPreviewData previewData, final Map<Object, Exception> suppressedExceptions)
    {
        final Blueprint blueprint = previewData.getBlueprint();
        final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        final Map<BlockPos, BlockEntity> tileEntitiesMap = BlueprintUtils.instantiateTileEntities(blueprint, blockAccess);
        entities = BlueprintUtils.instantiateEntities(blueprint, blockAccess);

        blockAccess.setBlockEntities(tileEntitiesMap);
        blockAccess.setEntities(entities);
        blockAccess.setSolidSubstitutionOverride(previewData.getSolidSubstitutionOverride());
        blockAccess.setRenderBlocksNiceOverride(previewData.getRenderBlocksNice());

        final PoseStack matrixStack = new PoseStack();
        matrixStack.translate(0.001, 0.001, 0.001);

        final ChunkOffsetBufferBuilderWrapper fluidBufferWrapper = new ChunkOffsetBufferBuilderWrapper();
        final Map<ChunkSectionLayer, ByteBufferBuilder> byteBuffers = new EnumMap<>(ChunkSectionLayer.class);
        final Map<ChunkSectionLayer, BufferBuilder> chunkBuffers = new EnumMap<>(ChunkSectionLayer.class);
        for (final ChunkSectionLayer layer : LAYERS)
        {
            final ByteBufferBuilder bytes = new ByteBufferBuilder(1 << 16);
            byteBuffers.put(layer, bytes);
            chunkBuffers.put(layer, new BufferBuilder(bytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK));
        }

        try
        {
            for (final BlockInfo blockInfo : blueprint.getBlockInfoAsList())
            {
                final BlockPos blockPos = blockInfo.getPos();
                BlockState state = blockInfo.getState();
                // specially handle blockTagSub here cuz of block entity changes
                if (previewData.getRenderBlocksNice() && state.getBlock() == ModBlocks.blockTagSubstitution.get())
                {
                    if (tileEntitiesMap.remove(blockPos) instanceof final BlockEntityTagSubstitution tagTE)
                    {
                        final CapturedBlock replacement = tagTE.getReplacement();
                        state = replacement.blockState();

                        replacement.serializedBE()
                            .map(tag -> BlockEntity.loadStatic(blockPos, replacement.blockState(), tag, blueprint.getRegistryAccess()))
                            .ifPresent(newBe -> {
                                newBe.setLevel(blockAccess);
                                tileEntitiesMap.put(blockPos, newBe);
                            });
                    }
                    else
                    {
                        state = Blocks.AIR.defaultBlockState();
                    }
                }
                else
                {
                    state = blockAccess.prepareBlockStateForRendering(state, blockPos);
                }

                final FluidState fluidState = state.getFluidState();
                try
                {
                    if (!fluidState.isEmpty())
                    {
                        final ChunkSectionLayer layer = ItemBlockRenderTypes.getRenderLayer(fluidState);
                        final int chunkOffsetX = blockPos.getX() - (blockPos.getX() & 15),
                            chunkOffsetY = blockPos.getY() - (blockPos.getY() & 15),
                            chunkOffsetZ = blockPos.getZ() - (blockPos.getZ() & 15);

                        fluidBufferWrapper.setOffset(chunkBuffers.get(layer), chunkOffsetX, chunkOffsetY, chunkOffsetZ);
                        blockRenderer.renderLiquid(blockPos, blockAccess, fluidBufferWrapper, state, fluidState);
                    }

                    if (state.getRenderShape() != RenderShape.INVISIBLE)
                    {
                        matrixStack.pushPose();
                        matrixStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                        // Fabric's entry point: handles emitQuads models and models with quads on several layers
                        blockRenderer.getModelRenderer().render(blockAccess,
                            blockRenderer.getBlockModel(state),
                            state,
                            blockPos,
                            matrixStack,
                            chunkBuffers::get,
                            true,
                            state.getSeed(blockPos),
                            OverlayTexture.NO_OVERLAY);
                        matrixStack.popPose();
                    }
                }
                catch (final ReportedException e)
                {
                    suppressedExceptions.put(blockInfo, e);
                }
            }

            clearMeshes();
            meshes = new EnumMap<>(ChunkSectionLayer.class);
            for (final ChunkSectionLayer layer : LAYERS)
            {
                try (MeshData meshData = chunkBuffers.get(layer).build())
                {
                    if (meshData != null)
                    {
                        final GpuBuffer gpuBuffer = RenderSystem.getDevice()
                            .createBuffer(() -> "Structurize blueprint " + layer.label(), GpuBuffer.USAGE_VERTEX, meshData.vertexBuffer());
                        meshes.put(layer, new Mesh(gpuBuffer, meshData.drawState().indexCount(), meshData.drawState().mode()));
                    }
                }
            }
        }
        finally
        {
            byteBuffers.values().forEach(ByteBufferBuilder::close);
            blockAccess.setSolidSubstitutionOverride(null);
            blockAccess.setRenderBlocksNiceOverride(Structurize.getConfig().getClient().renderPlaceholdersNice.get());
        }

        tileEntities = new ArrayList<>(tileEntitiesMap.values());
    }

    /**
     * Draws structure into world.
     */
    public void draw(final BlueprintPreviewData previewData, final BlockPos pos, final RenderLevelStage ctx)
    {
        // we've crashed hard before, full skip
        if (crashingObjects == null)
        {
            return;
        }

        try
        {
            final Map<Object, Exception> suppressedExceptions = drawUnsafe(previewData, pos, ctx);
            if (!suppressedExceptions.isEmpty())
            {
                if (!hasWarnedExceptions)
                {
                    hasWarnedExceptions = true;
                    Minecraft.getInstance().player.displayClientMessage(Component.translatable("structurize.preview_renderer.exception"), false);
                }

                boolean crashReported = false;
                boolean isEmpty = true;
                for (final Map.Entry<Object, Exception> e : suppressedExceptions.entrySet())
                {
                    if (!crashingObjects.add(e.getKey()))
                    {
                        continue;
                    }
                    isEmpty = false;

                    if (e.getValue() instanceof final ReportedException reportedException)
                    {
                        printCrashReport(reportedException.getReport(), previewData);
                        crashReported = true;
                    }
                    else
                    {
                        LOGGER.error("", e.getValue());
                    }
                }

                if (!crashReported && !isEmpty)
                {
                    printCrashReport(CrashReport.forThrowable(new Exception(), "Small exception, rendering partially"), previewData);
                }
            }
        }
        catch (final Exception e)
        {
            printCrashReport(CrashReport.forThrowable(e, "Fatal exception, cannot render"), previewData);

            crashingObjects = null;
            Minecraft.getInstance().player.displayClientMessage(
                Component.translatable("structurize.preview_renderer.cannot_render", previewData.getBlueprint().getName()), false);
        }
    }

    private static void printCrashReport(final CrashReport report, final BlueprintPreviewData previewData)
    {
        previewData.getBlueprint().describeSelfInCrashReport(report.addCategory("Blueprint"));
        LOGGER.error(report.getFriendlyReport(new ReportType("Problem during blueprint rendering", ReportType.TEST.nuggets())));
    }

    /**
     * Draws structure into world: block entities + entities when the context can take submissions, the cached block
     * mesh otherwise.
     *
     * @return suppressed exceptions
     */
    public Map<Object, Exception> drawUnsafe(final BlueprintPreviewData previewData, final BlockPos pos, final RenderLevelStage ctx)
    {
        final BlockPos anchorPos = pos.subtract(previewData.getBlueprint().getPrimaryBlockOffset());

        // cull entire rendering
        if (!ctx.getFrustum().isVisible(previewData.getBlueprint().getAABB().move(anchorPos)) && !bypassMainFrustum)
        {
            return Map.of();
        }

        final Map<Object, Exception> suppressedExceptions = new IdentityHashMap<>();
        final ProfilerFiller profiler = Profiler.get();

        profiler.push("struct_render_init");
        // make sure instances are synced
        updateBlueprint(previewData);
        blockAccess.setWorldPos(anchorPos);
        if (meshes == null)
        {
            init(previewData, suppressedExceptions);
        }

        final Vec3 realRenderRootVecd = Vec3.atLowerCornerOf(anchorPos).subtract(ctx.getCameraPosition());

        if (ctx.getSubmitNodeCollector() != null)
        {
            profiler.popPush("struct_render_entities");
            submitEntitiesAndBlockEntities(ctx, anchorPos, realRenderRootVecd, suppressedExceptions);
        }
        else
        {
            profiler.popPush("struct_render_blocks");
            drawBlocks(realRenderRootVecd.toVector3f(), previewData);
        }

        profiler.pop();
        return suppressedExceptions;
    }

    private void submitEntitiesAndBlockEntities(final RenderLevelStage ctx,
        final BlockPos anchorPos,
        final Vec3 realRenderRootVecd,
        final Map<Object, Exception> suppressedExceptions)
    {
        final Minecraft mc = Minecraft.getInstance();
        final SubmitNodeCollector collector = ctx.getSubmitNodeCollector();
        final PoseStack matrixStack = ctx.getPoseStack();
        final long gameTime = mc.level.getGameTime();
        final float partialTicks = ctx.getPartialTick().getGameTimeDeltaPartialTick(!mc.level.tickRateManager().isFrozen());
        final Vec3 localCamera = ctx.getCameraPosition().subtract(anchorPos.getX(), anchorPos.getY(), anchorPos.getZ());
        bypassMainFrustum = false;

        // Entities
        final EntityRenderDispatcher entityDispatcher = mc.getEntityRenderDispatcher();
        for (final Entity entity : entities)
        {
            if (!ctx.getFrustum().isVisible(entity.getBoundingBox().move(anchorPos)))
            {
                continue;
            }

            if (gameTime != lastGameTime && entity.getType().is(ModTags.PREVIEW_TICKING_ENTITIES))
            {
                try
                {
                    entity.tick();
                }
                catch (final Exception e)
                {
                    // well, noop
                    suppressedExceptions.put(entity, e);
                }
            }

            try
            {
                final EntityRenderState state = entityDispatcher.extractEntity(entity, partialTicks);
                entityDispatcher.submit(state,
                    ctx.cameraRenderState(),
                    state.x + realRenderRootVecd.x(),
                    state.y + realRenderRootVecd.y(),
                    state.z + realRenderRootVecd.z(),
                    matrixStack,
                    collector);
            }
            catch (final Exception e)
            {
                // Oops
                suppressedExceptions.put(entity, e);
            }
        }

        // Block entities
        final BlockEntityRenderDispatcher beDispatcher = mc.getBlockEntityRenderDispatcher();
        for (final BlockEntity tileEntity : tileEntities)
        {
            final BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer = beDispatcher.getRenderer(tileEntity);
            if (renderer == null || !renderer.shouldRender(tileEntity, localCamera))
            {
                continue;
            }

            final BlockPos tePos = tileEntity.getBlockPos();
            if (gameTime != lastGameTime)
            {
                tickClientSide(tileEntity, tePos, anchorPos, mc);
            }

            bypassMainFrustum |= renderer.shouldRenderOffScreen();
            if (!renderer.shouldRenderOffScreen() && !ctx.getFrustum().isVisible(new AABB(anchorPos.offset(tePos)).inflate(1.0)))
            {
                continue;
            }

            matrixStack.pushPose();
            matrixStack.translate(realRenderRootVecd.x() + tePos.getX(), realRenderRootVecd.y() + tePos.getY(), realRenderRootVecd.z() + tePos.getZ());
            try
            {
                final BlockEntityRenderState state = renderer.createRenderState();
                renderer.extractRenderState(tileEntity, state, partialTicks, localCamera, null);
                beDispatcher.submit(state, matrixStack, collector, ctx.cameraRenderState());
            }
            catch (final Exception e)
            {
                suppressedExceptions.put(tileEntity, e);
            }
            matrixStack.popPose();
        }

        lastGameTime = gameTime;
    }

    /**
     * hooks from EntityBlock#getTicker(Level, BlockState, BlockEntityType) for client side
     * either mc.level and anchorPos - particles, player distance etc.
     * or blockAccess and tePos - blueprint neighborhood
     */
    private void tickClientSide(final BlockEntity tileEntity, final BlockPos tePos, final BlockPos anchorPos, final Minecraft mc)
    {
        if (tileEntity instanceof final SpawnerBlockEntity spawner)
        {
            SpawnerBlockEntity.clientTick(mc.level, anchorPos.offset(tePos), blockAccess.getBlockState(tePos), spawner);
        }
        else if (tileEntity instanceof final EnchantingTableBlockEntity enchTable)
        {
            EnchantingTableBlockEntity.bookAnimationTick(mc.level, anchorPos.offset(tePos), blockAccess.getBlockState(tePos), enchTable);
        }
        else if (tileEntity instanceof final CampfireBlockEntity campfire)
        {
            final BlockState bs = blockAccess.getBlockState(tePos);
            if (bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT))
            {
                CampfireBlockEntity.particleTick(mc.level, anchorPos.offset(tePos), bs, campfire);
            }
        }
        else if (tileEntity instanceof final SkullBlockEntity skull)
        {
            final BlockState bs = blockAccess.getBlockState(tePos);
            if (bs.getBlock() instanceof SkullBlock && (bs.is(Blocks.DRAGON_HEAD) || bs.is(Blocks.DRAGON_WALL_HEAD) ||
                bs.is(Blocks.PIGLIN_HEAD) ||
                bs.is(Blocks.PIGLIN_WALL_HEAD)))
            {
                SkullBlockEntity.animation(blockAccess, tePos, bs, skull);
            }
        }
        else if (tileEntity instanceof final BeaconBlockEntity beacon)
        {
            // uses sound and applies buffs, but we dont want any of this since we're preview
            BeaconBlockEntity.tick(blockAccess, tePos, blockAccess.getBlockState(tePos), beacon);
        }
        else if (tileEntity instanceof final VaultBlockEntity vault)
        {
            VaultBlockEntity.Client.tick(mc.level, anchorPos.offset(tePos), blockAccess.getBlockState(tePos), vault.getClientData(), vault.getSharedData());
        }
        else if (tileEntity instanceof final TrialSpawnerBlockEntity trialSpawner)
        {
            trialSpawner.getTrialSpawner().tickClient(mc.level, anchorPos.offset(tePos), blockAccess.getBlockState(tePos).getOptionalValue(TrialSpawnerBlock.OMINOUS).orElse(false));
        }
    }

    private void drawBlocks(final Vector3f realRenderRoot, final BlueprintPreviewData previewData)
    {
        final float alpha = TransparencyHack.alpha(previewData.getOverridePreviewTransparency());
        final boolean blended = alpha < TransparencyHack.THRESHOLD;

        for (final ChunkSectionLayer layer : LAYERS)
        {
            final Mesh mesh = meshes.get(layer);
            if (mesh == null || mesh.indexCount() == 0)
            {
                continue;
            }

            final RenderType renderType = switch (layer)
            {
                case SOLID -> RenderTypes.solidMovingBlock();
                case CUTOUT -> RenderTypes.cutoutMovingBlock();
                case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
                case TRIPWIRE -> RenderTypes.tripwireMovingBlock();
            };
            RenderPipeline pipeline = renderType.pipeline();
            if (blended && layer == ChunkSectionLayer.SOLID)
            {
                pipeline = SOLID_BLENDED;
            }
            else if (blended && layer == ChunkSectionLayer.CUTOUT)
            {
                pipeline = CUTOUT_BLENDED;
            }
            drawMesh(mesh, renderType, pipeline, realRenderRoot, blended ? alpha : 1.0F);
        }
    }

    private static void drawMesh(final Mesh mesh, final RenderType renderType, final RenderPipeline pipeline, final Vector3f offset, final float alpha)
    {
        final RenderSetup setup = renderType.state;
        final GpuBufferSlice transform = RenderSystem.getDynamicUniforms()
            .writeTransform(RenderSystem.getModelViewMatrix(), new Vector4f(1.0F, 1.0F, 1.0F, alpha), offset, new Matrix4f());
        final RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(mesh.mode());
        final GpuBuffer indexBuffer = indices.getBuffer(mesh.indexCount());

        final RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        final GpuTextureView color = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : target.getColorTextureView();
        final GpuTextureView depth = target.useDepth
            ? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : target.getDepthTextureView())
            : null;

        try (RenderPass pass = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(() -> "Structurize blueprint preview", color, OptionalInt.empty(), depth, OptionalDouble.empty()))
        {
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transform);
            pass.setVertexBuffer(0, mesh.vertices());
            for (final Map.Entry<String, RenderSetup.TextureAndSampler> entry : setup.getTextures().entrySet())
            {
                pass.bindTexture(entry.getKey(), entry.getValue().textureView(), entry.getValue().sampler());
            }
            pass.setIndexBuffer(indexBuffer, indices.type());
            pass.drawIndexed(0, 0, mesh.indexCount(), 1);
        }
    }

    /**
     * Frees the GPU buffers.
     */
    private void clearMeshes()
    {
        if (meshes != null)
        {
            meshes.values().forEach(Mesh::close);
            meshes = null;
        }
    }

    @Override
    public void close()
    {
        clearMeshes();
    }

    /**
     * Preview transparency. 1.21.1 abused the GL blend colour; now it's the ColorModulator alpha of a blended pipeline.
     */
    public static class TransparencyHack
    {
        public static final float THRESHOLD = 0.99f;

        /**
         * @param overrideValue per-preview override, -1 for the client config value
         * @return alpha to render with, {@code >= THRESHOLD} meaning opaque
         */
        public static float alpha(final float overrideValue)
        {
            float alpha = Structurize.getConfig().getClient().rendererTransparency.get().floatValue();
            if (overrideValue != -1)
            {
                alpha = Mth.clamp(overrideValue, 0, 1);
            }
            return alpha < 0 ? 1.0F : alpha;
        }
    }
}
