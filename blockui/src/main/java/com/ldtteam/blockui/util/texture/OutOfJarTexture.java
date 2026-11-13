package com.ldtteam.blockui.util.texture;

import com.ldtteam.blockui.mod.BlockUI;
import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.ldtteam.fabric.Env;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * Texture loaded from a file outside any resource pack.
 * <p>
 * Fabric 1.21.11 port: textures are GPU objects now (GpuTexture); the image is read once and handed to vanilla's
 * {@link DynamicTexture}, which uploads it. The texture's mcmeta blur / clamp flags pick the sampler. Animated
 * (.mcmeta "animation") files show their first frame - see {@link SpriteTexture}.
 */
public class OutOfJarTexture extends DynamicTexture
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OutOfJarTexture.class);

    protected final OutOfJarResourceLocation resourceLocation;

    protected OutOfJarTexture(final OutOfJarResourceLocation resourceLocation, final NativeImage image, final Optional<TextureMetadataSection> meta)
    {
        super(resourceLocation::toString, image);
        this.resourceLocation = resourceLocation;
        meta.ifPresent(m -> {
            final AddressMode address = m.clamp() ? AddressMode.CLAMP_TO_EDGE : AddressMode.REPEAT;
            final FilterMode filter = m.blur() ? FilterMode.LINEAR : FilterMode.NEAREST;
            this.sampler = RenderSystem.getSamplerCache().getSampler(address, address, filter, filter, false);
        });
    }

    /**
     * Reads the file; returns an animated sprite's first frame as a {@link SpriteTexture}, anything else as a plain one.
     */
    static OutOfJarTexture load(final OutOfJarResourceLocation resLoc, final ResourceManager resourceManager) throws IOException
    {
        final Resource resource = OutOfJarResourceLocation.getResourceHandle(resLoc, resourceManager);
        final Optional<AnimationMetadataSection> animation = resource.metadata().getSection(AnimationMetadataSection.TYPE);
        final Optional<TextureMetadataSection> textureMeta = resource.metadata().getSection(TextureMetadataSection.TYPE);

        NativeImage image;
        try (var is = resource.open())
        {
            image = NativeImage.read(is);
        }

        if (animation.isPresent())
        {
            final FrameSize frame = animation.get().calculateFrameSize(image.getWidth(), image.getHeight());
            if (frame.width() < image.getWidth() || frame.height() < image.getHeight())
            {
                final NativeImage first = new NativeImage(frame.width(), frame.height(), false);
                image.copyRect(first, 0, 0, 0, 0, frame.width(), frame.height(), false, false);
                image.close();
                image = first;
            }
            return new SpriteTexture(resLoc, image, textureMeta);
        }
        return new OutOfJarTexture(resLoc, image, textureMeta);
    }

    public static AbstractTexture assertLoadedDefaultManagers(final Identifier resLoc)
    {
        return assertLoaded(resLoc, Minecraft.getInstance().getTextureManager(), Minecraft.getInstance().getResourceManager());
    }

    /**
     * Checks whether given resLoc should be loaded into given textureManager as an out-of-jar texture
     *
     * @return valid texture instance (including missing texture)
     */
    public static AbstractTexture assertLoaded(final Identifier resLoc, final TextureManager textureManager, final ResourceManager resourceManager)
    {
        if (!(resLoc instanceof final OutOfJarResourceLocation outOfJarResLoc))
        {
            // if not out-of-jar use normal vanilla systems
            return textureManager.getTexture(resLoc);
        }

        final AbstractTexture current = TextureLookup.peek(textureManager, resLoc);
        if (current != null)
        {
            return current;
        }

        if (!OutOfJarResourceLocation.fileExists(outOfJarResLoc, resourceManager))
        {
            if (!Env.PRODUCTION && !resLoc.getNamespace().equals(BlockUI.MOD_ID))
            {
                throw new IllegalArgumentException("Missing texture: " + resLoc);
            }
            return textureManager.getTexture(MissingTextureAtlasSprite.getLocation());
        }

        try
        {
            final OutOfJarTexture texture = load(outOfJarResLoc, resourceManager);
            textureManager.register(outOfJarResLoc, texture);
            return texture;
        }
        catch (final IOException e)
        {
            LOGGER.warn("Failed to load out-of-jar texture: " + resLoc, e);
            return textureManager.getTexture(MissingTextureAtlasSprite.getLocation());
        }
    }
}
