package com.minecolonies.core.client.render.worldevent;

import com.minecolonies.api.util.constant.Constants;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

/**
 * Fabric 1.21.11 port: the composite-state render types became a RenderPipeline plus a RenderSetup.
 */
public class RenderTypes
{
    /**
     * Flat textured quads, translucent, drawn through the world (status icons above citizens).
     */
    private static final RenderPipeline WORLD_ENTITY_ICON_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pipeline/entity_icon"))
        .withVertexShader("core/position_tex")
        .withFragmentShader("core/position_tex")
        .withSampler("Sampler0")
        .withBlend(BlendFunction.TRANSLUCENT)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withCull(false)
        .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
        .build();

    private static final Function<Identifier, RenderType> WORLD_ENTITY_ICON = Util.memoize(texture ->
        RenderType.create("minecolonies_entity_icon", RenderSetup.builder(WORLD_ENTITY_ICON_PIPELINE).withTexture("Sampler0", texture).bufferSize(1024).createRenderSetup()));

    /**
     * Usable for rendering simple flat textures
     *
     * @param  resLoc texture location
     * @return        render type
     */
    public static RenderType worldEntityIcon(final Identifier resLoc)
    {
        return WORLD_ENTITY_ICON.apply(resLoc);
    }
}
