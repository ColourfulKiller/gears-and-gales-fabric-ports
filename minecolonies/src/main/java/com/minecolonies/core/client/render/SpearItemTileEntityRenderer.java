package com.minecolonies.core.client.render;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.model.SpearModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * The spear's 3D item model. Fabric 1.21.11 port: the NeoForge BlockEntityWithoutLevelRenderer became a vanilla
 * special model renderer ("minecolonies:spear"), referenced from assets/minecolonies/items/spear.json.
 */
@Environment(EnvType.CLIENT)
public class SpearItemTileEntityRenderer implements NoDataSpecialModelRenderer
{
    public static final Identifier ID      = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "spear");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/entity/spear.png");

    private final SpearModel model;

    public SpearItemTileEntityRenderer(final SpearModel model)
    {
        this.model = model;
    }

    /** Registers the special model type; call once during client init. */
    public static void register()
    {
        SpecialModelRenderers.ID_MAPPER.put(ID, Unbaked.MAP_CODEC);
    }

    @Override
    public void submit(final ItemDisplayContext context, final PoseStack poseStack, final SubmitNodeCollector collector, final int light, final int overlay, final boolean foil, final int outline)
    {
        poseStack.pushPose();
        collector.submitModelPart(this.model.root(), poseStack, this.model.renderType(TEXTURE), light, overlay, null, false, foil, -1, null, outline);
        poseStack.popPose();
    }

    @Override
    public void getExtents(final Consumer<Vector3fc> consumer)
    {
        this.model.root().getExtentsForGui(new PoseStack(), consumer);
    }

    @Environment(EnvType.CLIENT)
    public record Unbaked() implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public MapCodec<Unbaked> type()
        {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(final SpecialModelRenderer.BakingContext context)
        {
            return new SpearItemTileEntityRenderer(new SpearModel(context.entityModelSet().bakeLayer(ModelLayers.TRIDENT)));
        }
    }
}