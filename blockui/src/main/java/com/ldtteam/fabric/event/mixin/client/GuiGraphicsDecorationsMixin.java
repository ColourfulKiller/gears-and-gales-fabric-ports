package com.ldtteam.fabric.event.mixin.client;

import com.ldtteam.fabric.event.client.RegisterItemDecorationsEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws RegisterItemDecorationsEvent decorators after vanilla's count / durability bar. */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsDecorationsMixin
{
    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At("TAIL"))
    private void ldtteam$decorations(final Font font, final ItemStack stack, final int x, final int y, @Nullable final String text, final CallbackInfo ci)
    {
        if (stack.isEmpty())
        {
            return;
        }
        for (final RegisterItemDecorationsEvent.IItemDecorator decorator : RegisterItemDecorationsEvent.decorators(stack.getItem()))
        {
            decorator.render((GuiGraphics) (Object) this, font, stack, x, y);
        }
    }
}