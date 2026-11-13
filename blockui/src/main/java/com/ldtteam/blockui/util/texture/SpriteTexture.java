package com.ldtteam.blockui.util.texture;

import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;

import java.util.Optional;

/**
 * Out-of-jar texture that came from an animated sprite file.
 * <p>
 * Fabric 1.21.11 port: shows the first frame. 1.21.1 ticked the animation itself through SpriteTicker / Tickable,
 * both of which were replaced by the GPU-side sprite animation of 1.21.5+.
 */
public class SpriteTexture extends OutOfJarTexture
{
    SpriteTexture(final OutOfJarResourceLocation resourceLocation, final NativeImage firstFrame, final Optional<TextureMetadataSection> meta)
    {
        super(resourceLocation, firstFrame, meta);
    }
}
