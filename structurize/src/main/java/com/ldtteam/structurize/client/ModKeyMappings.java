package com.ldtteam.structurize.client;

import com.ldtteam.fabric.Lazy;
import com.ldtteam.structurize.api.constants.Constants;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

import java.util.Set;

/**
 * Key mappings. Fabric port: NeoForge's key conflict contexts and modifiers don't exist; the blueprint-window keys are
 * only ever checked by {@link com.ldtteam.structurize.client.gui.AbstractBlueprintManipulationWindow} (so they can't
 * fire in game anyway), and the Shift requirement of the rotate keys is checked in {@link #isActiveAndMatches}.
 */
public class ModKeyMappings
{
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Constants.resLocStruct("general"));

    public static final Lazy<KeyMapping> TELEPORT = Lazy.of(() -> new KeyMapping("key.structurize.teleport",
            InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY));

    public static final Lazy<KeyMapping> MOVE_FORWARD = Lazy.of(() -> new KeyMapping("key.structurize.move_forward",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UP, CATEGORY));

    public static final Lazy<KeyMapping> MOVE_BACK = Lazy.of(() -> new KeyMapping("key.structurize.move_back",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, CATEGORY));

    public static final Lazy<KeyMapping> MOVE_LEFT = Lazy.of(() -> new KeyMapping("key.structurize.move_left",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, CATEGORY));

    public static final Lazy<KeyMapping> MOVE_RIGHT = Lazy.of(() -> new KeyMapping("key.structurize.move_right",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, CATEGORY));

    public static final Lazy<KeyMapping> MOVE_UP = Lazy.of(() -> new KeyMapping("key.structurize.move_up",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_ADD, CATEGORY));

    public static final Lazy<KeyMapping> MOVE_DOWN = Lazy.of(() -> new KeyMapping("key.structurize.move_down",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_SUBTRACT, CATEGORY));

    /** Shift + right arrow (Shift is implied, see {@link #SHIFT_REQUIRED}) */
    public static final Lazy<KeyMapping> ROTATE_CW = Lazy.of(() -> new KeyMapping("key.structurize.rotate_cw",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, CATEGORY));

    /** Shift + left arrow */
    public static final Lazy<KeyMapping> ROTATE_CCW = Lazy.of(() -> new KeyMapping("key.structurize.rotate_ccw",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, CATEGORY));

    public static final Lazy<KeyMapping> MIRROR = Lazy.of(() -> new KeyMapping("key.structurize.mirror",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, CATEGORY));

    public static final Lazy<KeyMapping> PLACE = Lazy.of(() -> new KeyMapping("key.structurize.place",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_ENTER, CATEGORY));

    private static final Set<Lazy<KeyMapping>> SHIFT_REQUIRED = Set.of(ROTATE_CW, ROTATE_CCW);

    public static void register()
    {
        KeyBindingHelper.registerKeyBinding(TELEPORT.get());
        KeyBindingHelper.registerKeyBinding(MOVE_FORWARD.get());
        KeyBindingHelper.registerKeyBinding(MOVE_BACK.get());
        KeyBindingHelper.registerKeyBinding(MOVE_LEFT.get());
        KeyBindingHelper.registerKeyBinding(MOVE_RIGHT.get());
        KeyBindingHelper.registerKeyBinding(MOVE_UP.get());
        KeyBindingHelper.registerKeyBinding(MOVE_DOWN.get());
        KeyBindingHelper.registerKeyBinding(ROTATE_CW.get());
        KeyBindingHelper.registerKeyBinding(ROTATE_CCW.get());
        KeyBindingHelper.registerKeyBinding(MIRROR.get());
        KeyBindingHelper.registerKeyBinding(PLACE.get());
    }

    /**
     * NeoForge's KeyMapping#isActiveAndMatches: the bound key, and Shift held exactly when the mapping wants it.
     */
    public static boolean isActiveAndMatches(final Lazy<KeyMapping> mapping, final int key)
    {
        final boolean shift = Minecraft.getInstance().hasShiftDown();
        return mapping.get().matches(new KeyEvent(key, 0, 0)) && shift == SHIFT_REQUIRED.contains(mapping);
    }

    private ModKeyMappings()
    {
        /*
         * Intentionally left empty.
         */
    }
}