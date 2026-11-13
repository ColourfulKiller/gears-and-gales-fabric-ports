package com.minecolonies.api.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import com.ldtteam.fabric.event.client.RegisterKeyMappingsEvent;
import com.ldtteam.fabric.Lazy;
import org.jetbrains.annotations.NotNull;

/**
 * Key mappings
 */
public class ModKeyMappings
{
    /** 1.21.9 key categories are registered ids; the label is "key.category.minecolonies.general". */
    private static final Lazy<KeyMapping.Category> CATEGORY = Lazy.of(() -> KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("minecolonies", "general")));

    /**
     * Toggle
     */
    public static final Lazy<KeyMapping> TOGGLE_GOGGLES = Lazy.of(() -> new KeyMapping("key.minecolonies.toggle_goggles",
            InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY.get()));

    /**
     * Register key mappings
     */
    public static void register(@NotNull final RegisterKeyMappingsEvent event)
    {
        event.register(TOGGLE_GOGGLES.get());
    }

    /**
     * Private constructor to hide the implicit one.
     */
    private ModKeyMappings()
    {
        /*
         * Intentionally left empty.
         */
    }
}
