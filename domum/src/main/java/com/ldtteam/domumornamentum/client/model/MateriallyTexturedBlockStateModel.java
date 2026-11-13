package com.ldtteam.domumornamentum.client.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Wraps the plain model of every materially textured block state. The block entity hands its {@link MaterialTextureData}
 * to the chunk mesher as render data ({@code RenderDataBlockEntity}); each model part is retextured once per distinct
 * texture data and cached (the 1.21.1 MateriallyTexturedBakedModel did the same with NeoForge ModelData).
 */
public class MateriallyTexturedBlockStateModel extends WrapperBlockStateModel
{
    private static final Direction[] SIDES = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null};

    private record Key(BlockModelPart part, MaterialTextureData data) {}

    private final Retexturer retexturer;
    private final Cache<Key, BlockModelPart> cache = CacheBuilder.newBuilder()
        .expireAfterAccess(2, TimeUnit.MINUTES)
        .concurrencyLevel(4)
        .maximumSize(10000)
        .build();

    public MateriallyTexturedBlockStateModel(final BlockStateModel wrapped, final Retexturer retexturer)
    {
        super(wrapped);
        this.retexturer = retexturer;
    }

    @Nullable
    private static MaterialTextureData dataAt(final BlockAndTintGetter blockView, final BlockPos pos)
    {
        return blockView.getBlockEntityRenderData(pos) instanceof final MaterialTextureData data && !data.isEmpty() ? data : null;
    }

    @Override
    public void emitQuads(final QuadEmitter emitter, final BlockAndTintGetter blockView, final BlockPos pos, final BlockState state,
        final RandomSource random, final Predicate<@Nullable Direction> cullTest)
    {
        final MaterialTextureData data = dataAt(blockView, pos);
        if (data == null)
        {
            super.emitQuads(emitter, blockView, pos, state, random, cullTest);
            return;
        }
        for (final BlockModelPart part : wrapped.collectParts(random))
        {
            retextured(part, data).emitQuads(emitter, cullTest);
        }
    }

    private BlockModelPart retextured(final BlockModelPart part, final MaterialTextureData data)
    {
        try
        {
            return cache.get(new Key(part, data), () -> {
                @SuppressWarnings("unchecked") final List<BakedQuad>[] quads = new List[SIDES.length];
                for (int i = 0; i < SIDES.length; i++)
                {
                    quads[i] = retexturer.retexture(part.getQuads(SIDES[i]), data, SIDES[i]);
                }
                final TextureAtlasSprite particle = Retexturer.particleFor(part.particleIcon(), data);
                return new RetexturedPart(quads, part.useAmbientOcclusion(), particle != null ? particle : part.particleIcon());
            });
        }
        catch (final ExecutionException e)
        {
            return part;
        }
    }

    @Override
    @Nullable
    public Object createGeometryKey(final BlockAndTintGetter blockView, final BlockPos pos, final BlockState state, final RandomSource random)
    {
        // geometry depends on the block entity's texture data, so it can't be shared between positions
        return null;
    }

    @Override
    public TextureAtlasSprite particleSprite(final BlockAndTintGetter blockView, final BlockPos pos, final BlockState state)
    {
        final MaterialTextureData data = dataAt(blockView, pos);
        final TextureAtlasSprite base = super.particleSprite(blockView, pos, state);
        if (data == null)
        {
            return base;
        }
        final TextureAtlasSprite particle = Retexturer.particleFor(base, data);
        return particle != null ? particle : base;
    }

    private record RetexturedPart(List<BakedQuad>[] quads, boolean useAmbientOcclusion, TextureAtlasSprite particleIcon) implements BlockModelPart
    {
        @Override
        public List<BakedQuad> getQuads(@Nullable final Direction direction)
        {
            return quads[direction == null ? 6 : direction.ordinal()];
        }
    }
}
