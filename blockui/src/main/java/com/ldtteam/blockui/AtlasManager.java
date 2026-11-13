package com.ldtteam.blockui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * GUI sprite lookup.
 * <p>
 * Fabric 1.21.11 port: 1.21.1 BlockUI built one extra texture atlas per mod (atlases/&lt;mod_id&gt;_gui.json). Since
 * 1.21.9 Minecraft only has a fixed list of atlases, so mods add their sprite folders to the vanilla GUI atlas
 * instead: ship assets/minecraft/atlases/gui.json with the same "directory" sources (atlas files from every pack are
 * merged). Sprite ids stay &lt;mod_id&gt;:&lt;path&gt;, so callers don't change.
 */
public class AtlasManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AtlasManager.class);
    public static final AtlasManager INSTANCE = new AtlasManager();

    private AtlasManager()
    {}

    /**
     * Kept for API compatibility; nothing to register any more (see class comment).
     */
    public void addAtlas(final Consumer<PreparableReloadListener> resourceRegistry, final String modId)
    {
        // intentionally empty
    }

    private static TextureAtlas guiAtlas()
    {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI);
    }

    /**
     * @return sprite for given resLoc from the GUI atlas (missing-texture sprite if absent)
     */
    public TextureAtlasSprite getSprite(final Identifier resLoc)
    {
        return guiAtlas().getSprite(resLoc);
    }

    /**
     * Dump the GUI atlas (now shared by all mods) into the given folder.
     */
    public void dumpAtlases(final Path dumpingFolder)
    {
        try
        {
            final TextureAtlas atlas = guiAtlas();
            atlas.dumpContents(atlas.location(), Files.createDirectories(dumpingFolder.resolve("gui")));
        }
        catch (final IOException e)
        {
            LOGGER.warn("Failed to dump gui atlas", e);
        }
    }

    /**
     * @return sprite scaling from given sprite
     */
    public static GuiSpriteScaling getSpriteScaling(final TextureAtlasSprite textureAtlasSprite)
    {
        return textureAtlasSprite.contents()
            .getAdditionalMetadata(GuiMetadataSection.TYPE)
            .orElse(GuiMetadataSection.DEFAULT)
            .scaling();
    }
}
