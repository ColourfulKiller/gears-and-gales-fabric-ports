package com.ldtteam.domumornamentum.client.model;

import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fabric port of the 1.21.1 RetexturedBakedModelBuilder + ModelSpriteQuadTransformer: swaps every quad whose sprite is
 * a texture-data component key for the matching face of the chosen block's model, keeping the quad's UVs relative to
 * the sprite. The tint index carries the chosen block's state id ({@code (stateId << 8) | tint}) so
 * {@link com.ldtteam.domumornamentum.client.color.MateriallyTexturedBlockBlockColor} can tint it like that block.
 * <p>
 * One instance per model bake ({@link #current()} is replaced on every resource reload), so the target-quad cache
 * never outlives the models it was read from.
 */
public final class Retexturer
{
    private static volatile Retexturer current = new Retexturer();

    public static Retexturer current()
    {
        return current;
    }

    /** Called at the start of every model reload. */
    public static Retexturer reset()
    {
        current = new Retexturer();
        return current;
    }

    private record TargetKey(Block block, @Nullable Direction cull, Direction face) {}

    public record Target(TextureAtlasSprite sprite, int tintIndex, Block block) {}

    private final Map<TargetKey, Optional<Target>> targets = new ConcurrentHashMap<>();

    private Retexturer()
    {
    }

    /**
     * @return the retextured quad, or the same quad when its sprite isn't a component of the data (or the chosen block
     * has no usable face)
     */
    public BakedQuad retexture(final BakedQuad quad, final MaterialTextureData data, @Nullable final Direction cull)
    {
        final Block block = data.getTexturedComponents().get(quad.sprite().contents().name());
        if (block == null)
        {
            return quad;
        }
        final Optional<Target> target = targets.computeIfAbsent(new TargetKey(block, cull, quad.direction()), Retexturer::findTarget);
        if (target.isEmpty())
        {
            return quad;
        }
        final TextureAtlasSprite from = quad.sprite();
        final TextureAtlasSprite to = target.get().sprite();
        final float uDelta = from.getU1() - from.getU0();
        final float vDelta = from.getV1() - from.getV0();
        final long[] uvs = new long[4];
        for (int i = 0; i < 4; i++)
        {
            final long packed = quad.packedUV(i);
            final float u = (UVPair.unpackU(packed) - from.getU0()) / uDelta;
            final float v = (UVPair.unpackV(packed) - from.getV0()) / vDelta;
            uvs[i] = UVPair.pack(to.getU(u), to.getV(v));
        }
        int tint = target.get().tintIndex();
        tint = 0 <= tint && tint <= 0xff ? (Block.getId(block.defaultBlockState()) << 8) | tint : -1;
        return new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(),
            uvs[0], uvs[1], uvs[2], uvs[3], tint, quad.direction(), to, quad.shade(), quad.lightEmission());
    }

    public List<BakedQuad> retexture(final List<BakedQuad> quads, final MaterialTextureData data, @Nullable final Direction cull)
    {
        if (quads.isEmpty())
        {
            return quads;
        }
        final List<BakedQuad> out = new ArrayList<>(quads.size());
        for (final BakedQuad quad : quads)
        {
            out.add(retexture(quad, data, cull));
        }
        return out;
    }

    /** The particle sprite of the block chosen for the given sprite's component, or {@code null}. */
    @Nullable
    public static TextureAtlasSprite particleFor(final TextureAtlasSprite sprite, final MaterialTextureData data)
    {
        final Block block = data.getTexturedComponents().get(sprite.contents().name());
        return block == null ? null : modelOf(block).particleIcon();
    }

    private static BlockStateModel modelOf(final Block block)
    {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(block.defaultBlockState());
    }

    /**
     * Like the original: the chosen block's first quad on the same cull side; if that's empty (e.g. the source quad is
     * unculled but the target's faces are culled) the first quad facing the source quad's direction.
     */
    private static Optional<Target> findTarget(final TargetKey key)
    {
        final List<BlockModelPart> parts = modelOf(key.block()).collectParts(RandomSource.create(42L));
        BakedQuad found = firstQuad(parts, key.cull());
        if (found == null)
        {
            found = firstQuad(parts, key.face());
        }
        return found == null ? Optional.empty() : Optional.of(new Target(found.sprite(), found.tintIndex(), key.block()));
    }

    @Nullable
    private static BakedQuad firstQuad(final List<BlockModelPart> parts, @Nullable final Direction direction)
    {
        for (final BlockModelPart part : parts)
        {
            final List<BakedQuad> quads = part.getQuads(direction);
            if (!quads.isEmpty())
            {
                return quads.get(0);
            }
        }
        return null;
    }
}
