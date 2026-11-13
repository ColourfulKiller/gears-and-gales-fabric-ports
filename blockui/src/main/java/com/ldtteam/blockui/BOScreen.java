package com.ldtteam.blockui;

import com.ldtteam.blockui.util.cursor.CursorUtils;
import com.ldtteam.blockui.views.BOWindow;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2f;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

/**
 * Wraps MineCrafts GuiScreen for BlockOut's Window.
 */
public class BOScreen extends Screen
{
    protected double renderScale = 1.0d;
    protected double mcScale = 1.0d;
    protected BOWindow window;
    protected double x = 0;
    protected double y = 0;
    public static boolean isMouseLeftDown = false;
    protected boolean isOpen = false;
    protected int framebufferWidth;
    protected int framebufferHeight;
    protected int absoluteMouseX;
    protected int absoluteMouseY;

    /**
     * Create a GuiScreen from a BlockOut window.
     *
     * @param w blockout window.
     */
    public BOScreen(final BOWindow w)
    {
        super(Component.literal("Blockout GUI"));
        window = w;
    }

    @Override
    public void render(final GuiGraphics ms, final int mx, final int my, final float f)
    {
        if (minecraft == null || !isOpen) // should never happen though
        {
            return;
        }

        absoluteMouseX = mx;
        absoluteMouseY = my;
        framebufferWidth = minecraft.getWindow().getWidth();
        framebufferHeight = minecraft.getWindow().getHeight();
        final int guiWidth = Math.max(framebufferWidth, 320);
        final int guiHeight = Math.max(framebufferHeight, 240);

        mcScale = minecraft.getWindow().getGuiScale();
        renderScale = window.getRenderType().calcRenderScale(minecraft.getWindow(), window);

        // NeoForge drew gui layers over the screen beneath; vanilla has one screen, so draw it ourselves
        final net.minecraft.client.gui.screens.Screen below = com.ldtteam.fabric.ScreenLayers.below(this);
        if (below != null)
        {
            below.render(ms, -1, -1, f);
        }

        if (window.hasLightbox() && minecraft.screen == this)
        {
            UiRenderMacros.fillGradient(ms, 0, 0, ms.guiWidth(), ms.guiHeight(), -1072689136, -804253680);
        }

        width = window.getWidth();
        height = window.getHeight();
        x = Math.floor((guiWidth - width * renderScale) / 2.0d);
        y = Math.floor((guiHeight - height * renderScale) / 2.0d);

        // BlockUI lays out in framebuffer pixels. 1.21.1 swapped the projection matrix for that; 1.21.6+ screens can't
        // touch it, so the same transform is the starting point of BlockUI's 2D matrix instead:
        // GUI units -> framebuffer pixels (1 / guiScale), then the window offset and BlockUI's own render scale.
        final Matrix3x2f pose = new Matrix3x2f(ms.pose())
            .scale((float) (1.0 / mcScale), (float) (1.0 / mcScale))
            .translate((float) x, (float) y)
            .scale((float) renderScale, (float) renderScale);

        try
        {
            final BOGuiGraphics target = new BOGuiGraphics(minecraft, pose, ms.guiRenderState, mx, my);
            window.draw(target, calcRelativeX(mx), calcRelativeY(my));

            if (minecraft.screen == this)
            {
                int debugX = (int) (-x / renderScale) + 3;
                if (Pane.debugging)
                {
                    debugX = target.drawString(
                        "XML: %s Scaling: %s (vanilla: %.2f our: %.2f) "
                            .formatted(window.getXmlResourceLocation(), window.getRenderType().name(), mcScale, renderScale),
                        debugX,
                        -minecraft.font.lineHeight,
                        Color.getByName("white"));
                }
                target.applyCursor(debugX);
            }

            window.drawLast(target, calcRelativeX(mx), calcRelativeY(my));
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "Rendering BO screen");
            final CrashReportCategory category = crashReport.addCategory("BO screen rendering details");
            category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
            category.setDetail("Scaling mode (window render type)", () -> window.getRenderType().name());
            category.setDetail("Vanilla gui scale", () -> Double.toString(mcScale));
            category.setDetail("BO gui scale", () -> Double.toString(renderScale));
            throw new ReportedException(crashReport);
        }
        finally
        {
            UiRenderMacros.resetTint();
        }
    }

    /**
     * 1.21.6+ draws a blurred, dimmed background before render(), sized by this screen's width/height - which BlockUI
     * sets to the window size, so it covered only part of the screen. BlockUI never drew a vanilla background (the
     * window's lightbox is drawn in render()), so skip it.
     */
    @Override
    public void renderBackground(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick)
    {
        // intentionally empty
    }

    @Override
    public boolean keyPressed(final KeyEvent event)
    {
        final int key = event.key();
        // keys without printable representation
        if (key >= 0 && key <= GLFW.GLFW_KEY_LAST)
        {
            try
            {
                return window.onKeyTyped('\0', key);
            }
            catch (final Exception e)
            {
                final CrashReport crashReport = CrashReport.forThrowable(e, "KeyPressed event for BO screen");
                final CrashReportCategory category = crashReport.addCategory("BO screen key event details");
                category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
                category.setDetail("GLFW key value", () -> Integer.toString(key));
                throw new ReportedException(crashReport);
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(final CharacterEvent event)
    {
        // BlockUI works in chars; characters outside the Basic Multilingual Plane (emoji etc.) are skipped
        if (!Character.isBmpCodePoint(event.codepoint()))
        {
            return false;
        }
        final char ch = (char) event.codepoint();
        try
        {
            return window.onKeyTyped(ch, event.modifiers());
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "CharTyped event for BO screen");
            final CrashReportCategory category = crashReport.addCategory("BO screen char event details");
            category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
            category.setDetail("Char value", () -> Character.toString(ch));
            throw new ReportedException(crashReport);
        }
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick)
    {
        final double mx = calcRelativeX(event.x());
        final double my = calcRelativeY(event.y());
        final int keyCode = event.button();
        try
        {
            if (keyCode == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                // Adjust coordinate to origin of window
                isMouseLeftDown = true;
                return window.click(mx, my);
            }
            else if (keyCode == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                return window.rightClick(mx, my);
            }
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "MousePressed event for BO screen");
            final CrashReportCategory category = crashReport.addCategory("BO screen mouse event details");
            category.setDetail("XML res loc", () -> Objects.toString(window.getXmlResourceLocation()));
            category.setDetail("GLFW mouse key value", () -> Integer.toString(keyCode));
            throw new ReportedException(crashReport);
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(final double mx, final double my, final double scrollHorizontalDiff, final double scrollVerticalDiff)
    {
        if (scrollVerticalDiff != 0)
        {
            try
            {
                return window.scrollInput(scrollHorizontalDiff * 10, scrollVerticalDiff * 10, calcRelativeX(mx), calcRelativeY(my));
            }
            catch (final Exception e)
            {
                final CrashReport crashReport = CrashReport.forThrowable(e, "MouseScroll event for BO screen");
                final CrashReportCategory category = crashReport.addCategory("BO screen scroll event details");
                category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
                category.setDetail("Scroll value", () -> Double.toString(scrollVerticalDiff));
                throw new ReportedException(crashReport);
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(final MouseButtonEvent event, final double deltaX, final double deltaY)
    {
        try
        {
            return window.onMouseDrag(calcRelativeX(event.x()), calcRelativeY(event.y()), event.button(), deltaX, deltaY);
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "MouseDragged event for BO screen");
            final CrashReportCategory category = crashReport.addCategory("BO screen mouse event details");
            category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
            throw new ReportedException(crashReport);
        }
    }

    @Override
    public boolean mouseReleased(final MouseButtonEvent event)
    {
        final int keyCode = event.button();
        if (keyCode == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            // Adjust coordinate to origin of window
            isMouseLeftDown = false;
            try
            {
                return window.onMouseReleased(calcRelativeX(event.x()), calcRelativeY(event.y()));
            }
            catch (final Exception e)
            {
                final CrashReport crashReport = CrashReport.forThrowable(e, "MouseReleased event for BO screen");
                final CrashReportCategory category = crashReport.addCategory("BO screen mouse event details");
                category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
                category.setDetail("GLFW mouse key value", () -> Integer.toString(keyCode));
                throw new ReportedException(crashReport);
            }
        }
        return false;
    }

    /**
     * Get the open window here.
     * @return the window.
     */
    public BOWindow getWindow()
    {
        return window;
    }

    @Override
    public void tick()
    {
        try
        {
            if (minecraft != null)
            {
                if (!isOpen)
                {
                    window.onOpened();
                    isOpen = true;
                }
                else
                {
                    window.onUpdate();

                    final LocalPlayer player = minecraft == null ? null : minecraft.player;
                    if (player != null && (!player.isAlive() || player.dead))
                    {
                        player.closeContainer();
                    }
                }
            }
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "Ticking/Updating BO screen");
            final CrashReportCategory category = crashReport.addCategory("BO screen update details");
            category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
            category.setDetail("Is opened", () -> Boolean.toString(isOpen));
            throw new ReportedException(crashReport);
        }
    }

    @Override
    public void removed()
    {
        try
        {
            window.onClosed();
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "Closing BO screen");
            final CrashReportCategory category = crashReport.addCategory("BO screen closing details");
            category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
            category.setDetail("Is opened", () -> Boolean.toString(isOpen));
            throw new ReportedException(crashReport);
        }
        finally
        {
            BOWindow.clearFocus();
            CursorUtils.resetCursor();
        }
    }

    @Override
    public boolean isPauseScreen()
    {
        return window.doesWindowPauseGame();
    }

    /**
     * Converts X from event to unscaled and unscrolled X for child in relative (top-left) coordinates.
     */
    private double calcRelativeX(final double xIn)
    {
        return (xIn * mcScale - x) / renderScale;
    }

    /**
     * Converts Y from event to unscaled and unscrolled Y for child in relative (top-left) coordinates.
     */
    private double calcRelativeY(final double yIn)
    {
        return (yIn * mcScale - y) / renderScale;
    }

    public double getRenderScale()
    {
        return renderScale;
    }

    public double getVanillaGuiScale()
    {
        return mcScale;
    }

    public int getFramebufferWidth()
    {
        return framebufferWidth;
    }

    public int getFramebufferHeight()
    {
        return framebufferHeight;
    }

    public int getAbsoluteMouseX()
    {
        return absoluteMouseX;
    }

    public int getAbsoluteMouseY()
    {
        return absoluteMouseY;
    }
}
