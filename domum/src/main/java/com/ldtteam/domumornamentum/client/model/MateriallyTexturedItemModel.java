package com.ldtteam.domumornamentum.client.model;

import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.domumornamentum.util.MaterialTextureDataUtil;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Item model type {@code domum_ornamentum:materially_textured} (used by the generated items/*.json): a plain block
 * model whose quads are retextured from the stack's texture data (random data when the stack has none, as the 1.21.1
 * MateriallyTexturedBakedModel did for creative tabs / JEI).
 * <p>
 * Item quads are tinted through the layer's tint array rather than block colours, so the packed
 * {@code (stateId << 8) | tint} index from {@link Retexturer} is remapped to a per-model array index here.
 */
public class MateriallyTexturedItemModel implements ItemModel
{
    private record Prepared(List<BakedQuad> quads, int[] tints, Supplier<Vector3fc[]> extents, boolean animated) {}

    private final List<BakedQuad> quads;
    private final ModelRenderProperties properties;
    private final Retexturer retexturer;
    private final Cache<MaterialTextureData, Prepared> cache = CacheBuilder.newBuilder()
        .expireAfterAccess(2, TimeUnit.MINUTES)
        .maximumSize(10000)
        .build();

    public MateriallyTexturedItemModel(final List<BakedQuad> quads, final ModelRenderProperties properties, final Retexturer retexturer)
    {
        this.quads = quads;
        this.properties = properties;
        this.retexturer = retexturer;
    }

    @Override
    public void update(final ItemStackRenderState renderState, final ItemStack stack, final ItemModelResolver resolver,
        final ItemDisplayContext displayContext, @Nullable final ClientLevel level, @Nullable final ItemOwner owner, final int seed)
    {
        MaterialTextureData data = MaterialTextureData.readFromItemStack(stack);
        if (data.isEmpty())
        {
            data = MaterialTextureDataUtil.generateRandomTextureDataFrom(stack);
        }
        final Prepared prepared = prepare(data);

        renderState.appendModelIdentityElement(this);
        renderState.appendModelIdentityElement(data);
        final ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
        if (stack.hasFoil())
        {
            layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
            renderState.setAnimated();
            renderState.appendModelIdentityElement(ItemStackRenderState.FoilType.STANDARD);
        }
        final int[] tints = layer.prepareTintLayers(prepared.tints().length);
        System.arraycopy(prepared.tints(), 0, tints, 0, tints.length);
        layer.setExtents(prepared.extents());
        layer.setRenderType(renderType(stack));
        this.properties.applyToLayer(layer, displayContext);
        layer.prepareQuadList().addAll(prepared.quads());
        if (prepared.animated())
        {
            renderState.setAnimated();
        }
    }

    private static RenderType renderType(final ItemStack stack)
    {
        if (stack.getItem() instanceof final BlockItem blockItem
            && ItemBlockRenderTypes.getChunkRenderType(blockItem.getBlock().defaultBlockState()) != ChunkSectionLayer.TRANSLUCENT)
        {
            return Sheets.cutoutBlockSheet();
        }
        return Sheets.translucentBlockItemSheet();
    }

    private Prepared prepare(final MaterialTextureData data)
    {
        try
        {
            return cache.get(data, () -> {
                final List<BakedQuad> out = new ArrayList<>(quads.size());
                final List<Integer> packedTints = new ArrayList<>();
                boolean animated = false;
                for (final BakedQuad quad : retexturer.retexture(quads, data, null))
                {
                    BakedQuad result = quad;
                    if (quad.tintIndex() != -1 && quad.tintIndex() > 0xff)
                    {
                        int index = packedTints.indexOf(quad.tintIndex());
                        if (index < 0)
                        {
                            index = packedTints.size();
                            packedTints.add(quad.tintIndex());
                        }
                        result = new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(),
                            quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(),
                            index, quad.direction(), quad.sprite(), quad.shade(), quad.lightEmission());
                    }
                    else if (quad.tintIndex() != -1)
                    {
                        // a tint the source model set itself (not a retextured one): no colour source for it
                        result = new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(),
                            quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(),
                            -1, quad.direction(), quad.sprite(), quad.shade(), quad.lightEmission());
                    }
                    animated |= result.sprite().contents().isAnimated();
                    out.add(result);
                }
                final int[] tints = new int[packedTints.size()];
                for (int i = 0; i < tints.length; i++)
                {
                    final int packed = packedTints.get(i);
                    final BlockState state = Block.stateById(packed >> 8);
                    final int color = Minecraft.getInstance().getBlockColors().getColor(state, null, null, packed & 0xff);
                    tints[i] = color == -1 ? -1 : ARGB.opaque(color);
                }
                final List<BakedQuad> finalQuads = List.copyOf(out);
                return new Prepared(finalQuads, tints, Suppliers.memoize(() -> BlockModelWrapper.computeExtents(finalQuads)), animated);
            });
        }
        catch (final ExecutionException e)
        {
            throw new IllegalStateException("Failed to retexture item model for " + data, e.getCause());
        }
    }

    public record Unbaked(Identifier model) implements ItemModel.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(Identifier.CODEC.fieldOf("model").forGetter(Unbaked::model)).apply(instance, Unbaked::new));

        @Override
        public void resolveDependencies(final ResolvableModel.Resolver resolver)
        {
            resolver.markDependency(this.model);
        }

        @Override
        public ItemModel bake(final ItemModel.BakingContext context)
        {
            final ModelBaker baker = context.blockModelBaker();
            final ResolvedModel resolved = baker.getModel(this.model);
            final TextureSlots slots = resolved.getTopTextureSlots();
            final List<BakedQuad> quads = resolved.bakeTopGeometry(slots, baker, BlockModelRotation.IDENTITY).getAll();
            return new MateriallyTexturedItemModel(quads, ModelRenderProperties.fromResolvedModel(baker, resolved, slots), Retexturer.current());
        }

        @Override
        public MapCodec<Unbaked> type()
        {
            return MAP_CODEC;
        }
    }
}
