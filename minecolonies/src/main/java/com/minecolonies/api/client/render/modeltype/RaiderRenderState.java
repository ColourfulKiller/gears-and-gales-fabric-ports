package com.minecolonies.api.client.render.modeltype;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Render state of a raider / camp mob (1.21.2+ entity rendering works on extracted states).
 */
public class RaiderRenderState extends HumanoidRenderState
{
    public int textureId;

    /** Mirrors the entity getter the renderers used to pick a texture. */
    public int getTextureId()
    {
        return textureId;
    }
}