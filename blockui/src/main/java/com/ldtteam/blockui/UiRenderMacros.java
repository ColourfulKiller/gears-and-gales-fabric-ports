package com.ldtteam.blockui;

import com.ldtteam.fabric.Env;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling.NineSlice;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling.Tile;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix3x2f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Our replacement for GuiComponent.
 * <p>
 * Fabric 1.21.11 port: screens can no longer draw straight to the GPU (Tesselator/BufferUploader and RenderSystem
 * blend/shader state were removed in 1.21.5-1.21.6). Every macro now records into {@link GuiGraphics}, which batches
 * and renders after the screen is done. Blending is part of the GUI render pipelines, and the old
 * RenderSystem.setShaderColor tint is the {@link #setTint} colour passed to every textured blit.
 */
public class UiRenderMacros
{
    public static final double HALF_BIAS = 0.5;

    /** ARGB tint applied to textured blits; replaces RenderSystem.setShaderColor. */
    private static int tint = -1;

    /** Replacement for UiRenderMacros.setTint(r, g, b, a). Reset with {@link #resetTint()}. */
    public static void setTint(final float red, final float green, final float blue, final float alpha)
    {
        tint = (Math.round(alpha * 255) & 0xff) << 24 | (Math.round(red * 255) & 0xff) << 16 | (Math.round(green * 255) & 0xff) << 8 |
            (Math.round(blue * 255) & 0xff);
    }

    public static void resetTint()
    {
        tint = -1;
    }

    public static int getTint()
    {
        return tint;
    }

    private static int argb(final int red, final int green, final int blue, final int alpha)
    {
        return (alpha & 0xff) << 24 | (red & 0xff) << 16 | (green & 0xff) << 8 | (blue & 0xff);
    }

    public static void drawLineRectGradient(final GuiGraphics g,
        final int x,
        final int y,
        final int w,
        final int h,
        final int argbColorStart,
        final int argbColorEnd)
    {
        drawLineRectGradient(g, x, y, w, h, argbColorStart, argbColorEnd, 1);
    }

    public static void drawLineRectGradient(final GuiGraphics g,
        final int x,
        final int y,
        final int w,
        final int h,
        final int argbColorStart,
        final int argbColorEnd,
        final int lineWidth)
    {
        if (lineWidth < 1 || ((argbColorStart >>> 24) == 0 && (argbColorEnd >>> 24) == 0))
        {
            return;
        }
        // top in the start colour, bottom in the end colour, sides as vertical gradients
        g.fill(x, y, x + w, y + lineWidth, argbColorStart);
        g.fill(x, y + h - lineWidth, x + w, y + h, argbColorEnd);
        g.fillGradient(x, y + lineWidth, x + lineWidth, y + h - lineWidth, argbColorStart, argbColorEnd);
        g.fillGradient(x + w - lineWidth, y + lineWidth, x + w, y + h - lineWidth, argbColorStart, argbColorEnd);
    }

    public static void drawLineRectGradient(final GuiGraphics g,
        final int x,
        final int y,
        final int w,
        final int h,
        final int redStart,
        final int redEnd,
        final int greenStart,
        final int greenEnd,
        final int blueStart,
        final int blueEnd,
        final int alphaStart,
        final int alphaEnd,
        final int lineWidth)
    {
        drawLineRectGradient(g, x, y, w, h,
            argb(redStart, greenStart, blueStart, alphaStart), argb(redEnd, greenEnd, blueEnd, alphaEnd), lineWidth);
    }

    public static void drawLineRect(final GuiGraphics g, final int x, final int y, final int w, final int h, final int argbColor)
    {
        drawLineRect(g, x, y, w, h, argbColor, 1);
    }

    public static void drawLineRect(final GuiGraphics g,
        final int x,
        final int y,
        final int w,
        final int h,
        final int argbColor,
        final int lineWidth)
    {
        if (lineWidth < 1 || (argbColor >>> 24) == 0)
        {
            return;
        }
        g.fill(x, y, x + w, y + lineWidth, argbColor);
        g.fill(x, y + h - lineWidth, x + w, y + h, argbColor);
        g.fill(x, y + lineWidth, x + lineWidth, y + h - lineWidth, argbColor);
        g.fill(x + w - lineWidth, y + lineWidth, x + w, y + h - lineWidth, argbColor);
    }

    public static void drawLineRect(final GuiGraphics g,
        final int x,
        final int y,
        final int w,
        final int h,
        final int red,
        final int green,
        final int blue,
        final int alpha,
        final int lineWidth)
    {
        drawLineRect(g, x, y, w, h, argb(red, green, blue, alpha), lineWidth);
    }

    public static void fill(final GuiGraphics g, final int x, final int y, final int w, final int h, final int argbColor)
    {
        if ((argbColor >>> 24) != 0)
        {
            g.fill(x, y, x + w, y + h, argbColor);
        }
    }

    public static void fill(final GuiGraphics g,
        final int x,
        final int y,
        final int w,
        final int h,
        final int red,
        final int green,
        final int blue,
        final int alpha)
    {
        fill(g, x, y, w, h, argb(red, green, blue, alpha));
    }

    public static void fillGradient(final GuiGraphics g,
        final int x,
        final int y,
        final int w,
        final int h,
        final int argbColorStart,
        final int argbColorEnd)
    {
        if ((argbColorStart >>> 24) != 0 || (argbColorEnd >>> 24) != 0)
        {
            g.fillGradient(x, y, x + w, y + h, argbColorStart, argbColorEnd);
        }
    }

    public static void fillGradient(final GuiGraphics g,
        final int x,
        final int y,
        final int w,
        final int h,
        final int redStart,
        final int redEnd,
        final int greenStart,
        final int greenEnd,
        final int blueStart,
        final int blueEnd,
        final int alphaStart,
        final int alphaEnd)
    {
        fillGradient(g, x, y, w, h, argb(redStart, greenStart, blueStart, alphaStart), argb(redEnd, greenEnd, blueEnd, alphaEnd));
    }

    public static void hLine(final GuiGraphics g, final int x, final int xEnd, final int y, final int argbColor)
    {
        line(g, x, y, xEnd, y, argbColor);
    }

    public static void hLine(final GuiGraphics g, final int x, final int xEnd, final int y, final int red, final int green, final int blue, final int alpha)
    {
        line(g, x, y, xEnd, y, argb(red, green, blue, alpha));
    }

    public static void vLine(final GuiGraphics g, final int x, final int y, final int yEnd, final int argbColor)
    {
        line(g, x, y, x, yEnd, argbColor);
    }

    public static void vLine(final GuiGraphics g, final int x, final int y, final int yEnd, final int red, final int green, final int blue, final int alpha)
    {
        line(g, x, y, x, yEnd, argb(red, green, blue, alpha));
    }

    public static void line(final GuiGraphics g, final int x, final int y, final int xEnd, final int yEnd, final int red, final int green, final int blue, final int alpha)
    {
        line(g, x, y, xEnd, yEnd, argb(red, green, blue, alpha));
    }

    /**
     * One-pixel line. The old version drew a GL line between the two points; the GUI renderer only takes rectangles, so
     * axis-aligned lines are one fill and diagonal ones are stepped (Bresenham) one pixel at a time.
     */
    public static void line(final GuiGraphics g, final int x, final int y, final int xEnd, final int yEnd, final int argbColor)
    {
        if ((argbColor >>> 24) == 0)
        {
            return;
        }
        if (y == yEnd)
        {
            g.fill(Math.min(x, xEnd), y, Math.max(x, xEnd), y + 1, argbColor);
            return;
        }
        if (x == xEnd)
        {
            g.fill(x, Math.min(y, yEnd), x + 1, Math.max(y, yEnd), argbColor);
            return;
        }
        int cx = x, cy = y;
        final int dx = Math.abs(xEnd - x), dy = -Math.abs(yEnd - y);
        final int sx = x < xEnd ? 1 : -1, sy = y < yEnd ? 1 : -1;
        int err = dx + dy;
        while (cx != xEnd || cy != yEnd)
        {
            g.fill(cx, cy, cx + 1, cy + 1, argbColor);
            final int e2 = 2 * err;
            if (e2 >= dy)
            {
                err += dy;
                cx += sx;
            }
            if (e2 <= dx)
            {
                err += dx;
                cy += sy;
            }
        }
    }

    public static void blit(final GuiGraphics g, final Identifier rl, final int x, final int y, final int w, final int h, final int u, final int v, final int mapW, final int mapH)
    {
        blit(g, rl, x, y, w, h, (float) u / mapW, (float) v / mapH, (float) (u + w) / mapW, (float) (v + h) / mapH);
    }

    public static void blit(final GuiGraphics g,
        final Identifier rl,
        final int x,
        final int y,
        final int w,
        final int h,
        final int u,
        final int v,
        final int uW,
        final int vH,
        final int mapW,
        final int mapH)
    {
        blit(g, rl, x, y, w, h, (float) u / mapW, (float) v / mapH, (float) (u + uW) / mapW, (float) (v + vH) / mapH);
    }

    public static void blitSprite(final GuiGraphics g, final TextureAtlasSprite sprite, final GuiSpriteScaling guiScaling, final int x, final int y, final int w, final int h)
    {
        resolveSprite(sprite, guiScaling).blit(g, x, y, w, h);
    }

    public static void blitSprite(final GuiGraphics g, final TextureAtlasSprite sprite, final int x, final int y, final int w, final int h)
    {
        blit(g, sprite.atlasLocation(), x, y, w, h, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
    }

    public static void blit(final GuiGraphics g, final Identifier rl, final int x, final int y, final int w, final int h)
    {
        blit(g, rl, x, y, w, h, 0.0f, 0.0f, 1.0f, 1.0f);
    }

    public static void blit(final GuiGraphics g,
        final Identifier rl,
        final int x,
        final int y,
        final int w,
        final int h,
        final float uMin,
        final float vMin,
        final float uMax,
        final float vMax)
    {
        // innerBlit takes x0, x1, y0, y1, u0, u1, v0, v1, colour (made public by blockui.accesswidener)
        g.innerBlit(RenderPipelines.GUI_TEXTURED, rl, x, x + w, y, y + h, uMin, uMax, vMin, vMax, tint);
    }

    /**
     * Draws texture without scaling so one texel is one pixel, using repeatable texture center. Each piece is one
     * blit (the old version built one vertex buffer for all of them).
     *
     * @param uRepeat         offset relative to u, v [texels], smaller than uWidth
     * @param vRepeat         offset relative to u, v [texels], smaller than vHeight
     * @param repeatWidth     size of repeatable part in texture [texels], smaller than or equal repeatBoxWidth - uRepeat
     * @param repeatHeight    size of repeatable part in texture [texels], smaller than or equal repeatBoxHeight - vRepeat
     * @param repeatBoxWidth  size of entire repeatable box (borders + repeat part) [texels]
     * @param repeatBoxHeight size of entire repeatable box (borders + repeat part) [texels]
     */
    protected static void blitRepeatable(final GuiGraphics g,
        final Identifier rl,
        final int x,
        final int y,
        final int width,
        final int height,
        final float uMin,
        final float vMin,
        final float uMax,
        final float vMax,
        final int uRepeat,
        final int vRepeat,
        final int repeatWidth,
        final int repeatHeight,
        final int repeatBoxWidth,
        final int repeatBoxHeight)
    {
        if (uRepeat < 0 || vRepeat < 0 ||
            uRepeat >= repeatBoxWidth ||
            vRepeat >= repeatBoxHeight ||
            repeatWidth < 1 ||
            repeatHeight < 1 ||
            repeatWidth > repeatBoxWidth - uRepeat ||
            repeatHeight > repeatBoxHeight - vRepeat)
        {
            throw new IllegalArgumentException("Repeatable box is outside of texture box");
        }

        final int repeatCountX = Math.max(1, Math.max(0, width - (repeatBoxWidth - repeatWidth)) / repeatWidth);
        final int repeatCountY = Math.max(1, Math.max(0, height - (repeatBoxHeight - repeatHeight)) / repeatHeight);
        final float uTexelWidth = (uMax - uMin) / repeatBoxWidth;
        final float vTexelHeight = (vMax - vMin) / repeatBoxHeight;

        // main
        for (int i = 0; i < repeatCountX; i++)
        {
            final int uAdjust = i == 0 ? 0 : uRepeat;
            final int xStart = x + uAdjust + i * repeatWidth;
            final int w = Math.min(repeatWidth + uRepeat - uAdjust, width - (repeatBoxWidth - uRepeat - repeatWidth));
            final float minU = uMin + uTexelWidth * uAdjust;
            final float maxU = minU + uTexelWidth * w;

            for (int j = 0; j < repeatCountY; j++)
            {
                final int vAdjust = j == 0 ? 0 : vRepeat;
                final int yStart = y + vAdjust + j * repeatHeight;
                final int h = Math.min(repeatHeight + vRepeat - vAdjust, height - (repeatBoxHeight - vRepeat - repeatHeight));
                final float minV = vMin + vTexelHeight * vAdjust;
                final float maxV = minV + vTexelHeight * h;

                blitQuad(g, rl, xStart, xStart + w, yStart, yStart + h, minU, maxU, minV, maxV);
            }
        }

        final int xEnd = x + Math.min(uRepeat + repeatCountX * repeatWidth, width - (repeatBoxWidth - uRepeat - repeatWidth));
        final int yEnd = y + Math.min(vRepeat + repeatCountY * repeatHeight, height - (repeatBoxHeight - vRepeat - repeatHeight));
        final int uLeft = width - (xEnd - x);
        final int vBot = height - (yEnd - y);
        final float restMinU = uMax - uLeft * uTexelWidth;
        final float restMinV = vMax - vBot * vTexelHeight;

        // bot border
        for (int i = 0; i < repeatCountX; i++)
        {
            final int uAdjust = i == 0 ? 0 : uRepeat;
            final int xStart = x + uAdjust + i * repeatWidth;
            final int w = Math.min(repeatWidth + uRepeat - uAdjust, width - uLeft);
            final float minU = uMin + uTexelWidth * uAdjust;
            final float maxU = minU + uTexelWidth * w;

            blitQuad(g, rl, xStart, xStart + w, yEnd, yEnd + vBot, minU, maxU, restMinV, vMax);
        }

        // left border
        for (int j = 0; j < repeatCountY; j++)
        {
            final int vAdjust = j == 0 ? 0 : vRepeat;
            final int yStart = y + vAdjust + j * repeatHeight;
            final int h = Math.min(repeatHeight + vRepeat - vAdjust, height - vBot);
            final float minV = vMin + vTexelHeight * vAdjust;
            final float maxV = minV + vTexelHeight * h;

            blitQuad(g, rl, xEnd, xEnd + uLeft, yStart, yStart + h, restMinU, uMax, minV, maxV);
        }

        // bot left corner
        blitQuad(g, rl, xEnd, xEnd + uLeft, yEnd, yEnd + vBot, restMinU, uMax, restMinV, vMax);
    }

    private static void blitQuad(final GuiGraphics g,
        final Identifier rl,
        final int xStart,
        final int xEnd,
        final int yStart,
        final int yEnd,
        final float uMin,
        final float uMax,
        final float vMin,
        final float vMax)
    {
        if (xEnd > xStart && yEnd > yStart)
        {
            g.innerBlit(RenderPipelines.GUI_TEXTURED, rl, xStart, xEnd, yStart, yEnd, uMin, uMax, vMin, vMax, tint);
        }
    }

    /**
     * Render an entity on a GUI.
     * <p>
     * 1.21.6+ draws entities in screens as a picture-in-picture element: the entity is rendered into its own texture
     * and placed in a screen-space box. That box does not follow the GUI matrix, so BlockUI's own scaling and offset
     * are applied to the box by hand here.
     *
     * @param x       horizontal center position
     * @param y       vertical bottom position
     * @param scale   scaling factor (GUI pixels per block)
     * @param headYaw adjusts look rotation
     * @param yaw     adjusts body rotation
     * @param pitch   adjusts look rotation
     * @param entity  the entity to render
     */
    public static void drawEntity(final GuiGraphics g,
        final int x,
        final int y,
        final double scale,
        final float headYaw,
        final float yaw,
        final float pitch,
        final Entity entity)
    {
        if (entity.level() == null)
        {
            return;
        }
        final EntityRenderer<? super Entity, ?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        final EntityRenderState state = renderer.createRenderState(entity, 1.0F);
        state.lightCoords = 0x00F000F0;
        state.shadowPieces.clear();
        state.outlineColor = 0;
        if (state instanceof final LivingEntityRenderState living)
        {
            living.bodyRot = 180.0F + yaw;
            living.yRot = headYaw - yaw;
            living.xRot = -pitch;
        }

        final float bbW = Math.max(state.boundingBoxWidth, 0.1F);
        final float bbH = Math.max(state.boundingBoxHeight, 0.1F);
        final float half = (float) (Math.max(bbW, bbH) * scale / 2.0);
        final float height = (float) (bbH * scale);

        // box corners in BlockUI space -> screen space
        final Matrix3x2f pose = new Matrix3x2f(g.pose());
        final Vector2f p0 = pose.transformPosition(new Vector2f(x - half, y - height));
        final Vector2f p1 = pose.transformPosition(new Vector2f(x + half, y));
        final float poseScale = (float) Math.sqrt(pose.m00 * pose.m00 + pose.m01 * pose.m01);

        final Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        final Quaternionf pitchRotation = new Quaternionf().rotateX((float) Math.toRadians(pitch));
        rotation.mul(pitchRotation);

        g.submitEntityRenderState(state,
            (float) scale * poseScale,
            new Vector3f(0.0F, bbH / 2.0F, 0.0F),
            rotation,
            pitchRotation,
            Math.round(Math.min(p0.x, p1.x)),
            Math.round(Math.min(p0.y, p1.y)),
            Math.round(Math.max(p0.x, p1.x)),
            Math.round(Math.max(p0.y, p1.y)));
    }

    /**
     * @return rendering lambda detached from sprite and guiScaling instances
     */
    public static ResolvedBlit resolveSprite(final TextureAtlasSprite sprite, final GuiSpriteScaling guiScaling)
    {
        final Identifier atlasLocation = sprite.atlasLocation();
        final float u0 = sprite.getU0();
        final float v0 = sprite.getV0();
        final float u1 = sprite.getU1();
        final float v1 = sprite.getV1();
        if (guiScaling.type() == Type.STRETCH)
        {
            return (g, x, y, w, h) -> blit(g, atlasLocation, x, y, w, h, u0, v0, u1, v1);
        }
        else if (guiScaling instanceof final NineSlice nineSlice)
        {
            final int rbW = nineSlice.width();
            final int rbH = nineSlice.height();
            final int uR = nineSlice.border().left();
            final int vR = nineSlice.border().top();
            final int rW = rbW - uR - nineSlice.border().right();
            final int rH = rbH - vR - nineSlice.border().bottom();

            return (g, x, y, w, h) -> {
                if (rbW == w && rbH == h)
                {
                    blit(g, atlasLocation, x, y, w, h, u0, v0, u1, v1);
                }
                else
                {
                    blitRepeatable(g, atlasLocation, x, y, w, h, u0, v0, u1, v1, uR, vR, rW, rH, rbW, rbH);
                }
            };
        }
        else if (guiScaling instanceof final Tile tile)
        {
            final int tW = tile.width();
            final int tH = tile.height();

            return (g, x, y, w, h) -> {
                if (tW == w && tH == h)
                {
                    blit(g, atlasLocation, x, y, w, h, u0, v0, u1, v1);
                }
                else
                {
                    blitRepeatable(g, atlasLocation, x, y, w, h, u0, v0, u1, v1, 0, 0, tW, tH, tW, tH);
                }
            };
        }
        if (!Env.PRODUCTION)
        {
            throw new UnsupportedOperationException("Missing resolver for gui scaling: " + guiScaling.type());
        }
        return ResolvedBlit.EMPTY;
    }

    /**
     * Used for precompiling math around rendering
     */
    @FunctionalInterface
    public static interface ResolvedBlit
    {
        public static final ResolvedBlit EMPTY = (g, x, y, w, h) -> {};

        void blit(GuiGraphics g, int x, int y, int w, int h);
    }
}
