package com.ldtteam.blockui.util.texture;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric 1.21.11 port helper: 1.21.1 had TextureManager.getTexture(id, null), which returned null for an unregistered
 * id. 1.21.11's getTexture(id) loads (or registers the missing texture) as a side effect, which BlockUI must avoid
 * for out-of-jar files. This reads the manager's map directly (byPath, widened in blockui.accesswidener).
 */
public final class TextureLookup
{
    private TextureLookup() {}

    public static @Nullable AbstractTexture peek(final TextureManager textureManager, final Identifier id)
    {
        return textureManager.byPath.get(id);
    }
}
