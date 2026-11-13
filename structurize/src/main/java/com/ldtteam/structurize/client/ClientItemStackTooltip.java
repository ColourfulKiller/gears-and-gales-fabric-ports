package com.ldtteam.structurize.client;

import com.ldtteam.structurize.items.ItemStackTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Tooltip line showing an item icon and its name. Fabric 1.21.11 port: registered through Fabric's
 * TooltipComponentCallback; text is drawn with GuiGraphics now.
 */
public class ClientItemStackTooltip implements ClientTooltipComponent
{
    private final ItemStackTooltip component;

    public ClientItemStackTooltip(@NotNull final ItemStackTooltip component)
    {
        this.component = component;
    }

    @Override
    public int getHeight(@NotNull final Font font)
    {
        return 20;
    }

    @Override
    public int getWidth(@NotNull final Font font)
    {
        return 20 + font.width(this.component.getStack().getDisplayName().getVisualOrderText());
    }

    @Override
    public void renderText(@NotNull final GuiGraphics graphics, @NotNull final Font font, final int x, final int y)
    {
        graphics.drawString(font, this.component.getStack().getHoverName(), x + 20, y + (20 - font.lineHeight) / 2, 0xffffffff, false);
    }

    @Override
    public void renderImage(@NotNull final Font font, final int x, final int y, final int width, final int height, @NotNull final GuiGraphics graphics)
    {
        graphics.renderItem(this.component.getStack(), x + 2, y + 2);
        graphics.renderItemDecorations(font, this.component.getStack(), x + 2, y + 2);
    }
}