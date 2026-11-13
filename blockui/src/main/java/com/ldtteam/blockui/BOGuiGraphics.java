package com.ldtteam.blockui;

import com.ldtteam.blockui.mod.item.BlockStateRenderingData;
import com.ldtteam.blockui.util.cursor.Cursor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

/**
 * BlockUI's GuiGraphics.
 * <p>
 * Fabric 1.21.11 port: shares the screen's {@link GuiRenderState} but has its own 2D matrix stack, which starts out
 * in BlockUI's coordinate space (framebuffer pixels, offset and scaled by the window render scale). The 3D PoseStack
 * of 1.21.1 is gone from GUI rendering; pose() is now a {@link Matrix3x2fStack}.
 */
public class BOGuiGraphics extends GuiGraphics
{
    private int cursorMaxDepth = -1;
    private Cursor selectedCursor = Cursor.DEFAULT;
    private final Minecraft mc;

    public BOGuiGraphics(final Minecraft mc, final Matrix3x2f initialPose, final GuiRenderState renderState, final int mouseX, final int mouseY)
    {
        super(mc, new DepthTrackingStack(initialPose), renderState, mouseX, mouseY);
        this.mc = mc;
    }

    public Minecraft getMinecraft()
    {
        return mc;
    }

    public Font font()
    {
        return mc.font;
    }

    public void renderItemDecorations(final ItemStack itemStack, final int x, final int y)
    {
        super.renderItemDecorations(mc.font, itemStack, x, y);
    }

    public void renderItemDecorations(final ItemStack itemStack, final int x, final int y, @Nullable final String altStackSize)
    {
        super.renderItemDecorations(mc.font, itemStack, x, y, altStackSize);
    }

    public int drawString(final String text, final float x, final float y, final int color)
    {
        return drawString(text, x, y, color, false);
    }

    /**
     * Vanilla only takes whole-pixel positions now, so fractional positions are applied as a translation.
     *
     * @return x after the text, like the old GuiGraphics.drawString
     */
    public int drawString(final String text, final float x, final float y, final int color, final boolean shadow)
    {
        pose().pushMatrix();
        pose().translate(x, y);
        // GUI text is ARGB in 1.21.6+; a colour without an alpha byte would be invisible
        super.drawString(mc.font, text, 0, 0, (color >>> 24) == 0 ? color | 0xFF000000 : color, shadow);
        pose().popMatrix();
        return (int) x + mc.font.width(text);
    }

    private int depth()
    {
        return ((DepthTrackingStack) pose()).depth;
    }

    public void setCursor(final Cursor cursor)
    {
        if (depth() >= cursorMaxDepth)
        {
            cursorMaxDepth = depth();
            selectedCursor = cursor;
        }
    }

    /**
     * @param debugXoffset debug string x offset
     */
    public void applyCursor(final int debugXoffset)
    {
        selectedCursor.apply();

        if (Pane.debugging)
        {
            drawString(selectedCursor.toString(), debugXoffset, -mc.font.lineHeight, Color.getByName("white"));
        }
    }

    /**
     * Render given blockState like an item icon.
     * <p>
     * Fabric 1.21.11 port, first pass: draws the backing item instead of the exact block state, block entity and fluid.
     * 3D content inside a screen now has to go through a picture-in-picture renderer (Fabric API's
     * SpecialGuiElementRegistry); a dedicated block-state renderer is still to do.
     *
     * @param data      blockState rendering data
     * @param itemStack backing itemStack for given blockState
     */
    public void renderBlockStateAsItem(final BlockStateRenderingData data, final ItemStack itemStack)
    {
        renderItem(itemStack, 0, 0);
    }

    public static double getAltSpeedFactor()
    {
        return Minecraft.getInstance().hasAltDown() ? 5 : 1;
    }

    /** Matrix3x2fStack that remembers how deep it is (for the cursor priority logic above). */
    private static final class DepthTrackingStack extends Matrix3x2fStack
    {
        private int depth = 0;

        private DepthTrackingStack(final Matrix3x2f initial)
        {
            super(32);
            set(initial);
        }

        @Override
        public Matrix3x2fStack pushMatrix()
        {
            depth++;
            return super.pushMatrix();
        }

        @Override
        public Matrix3x2fStack popMatrix()
        {
            depth--;
            return super.popMatrix();
        }
    }
}
