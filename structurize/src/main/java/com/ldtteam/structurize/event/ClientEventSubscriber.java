package com.ldtteam.structurize.event;

import com.ldtteam.blockui.BOScreen;
import com.ldtteam.structurize.api.IScrollableItem;
import com.ldtteam.structurize.api.ISpecialBlockPickItem;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.client.BlueprintHandler;
import com.ldtteam.structurize.client.ModKeyMappings;
import com.ldtteam.structurize.client.gui.WindowExtendedBuildTool;
import com.ldtteam.structurize.items.ItemScanTool;
import com.ldtteam.structurize.network.messages.ItemMiddleMouseMessage;
import com.ldtteam.structurize.network.messages.ScanToolTeleportMessage;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Iterator;
import java.util.Map;

/**
 * Client game events (was the NeoForge game-bus subscriber); wired in
 * {@link com.ldtteam.structurize.client.StructurizeClient}, the scroll hook in a MouseHandler mixin.
 */
public class ClientEventSubscriber
{
    private ClientEventSubscriber()
    {
    }

    /**
     * The health and food bars are hidden while the extended build tool window is open.
     */
    public static boolean shouldHideSurvivalBars()
    {
        return Minecraft.getInstance().screen instanceof final BOScreen screen && screen.getWindow() instanceof WindowExtendedBuildTool;
    }

    public static void onClientTickEvent(final Minecraft mc)
    {
        Profiler.get().push("structurize");

        if (mc.level != null && mc.level.getGameTime() % (Constants.TICKS_SECOND * BlueprintHandler.CACHE_EXPIRE_CHECK_SECONDS) == 0)
        {
            Profiler.get().push("blueprint_manager_tick");
            BlueprintHandler.getInstance().cleanCache();
            Profiler.get().pop();
        }

        if (ModKeyMappings.TELEPORT.get().consumeClick() && mc.level != null && mc.player != null &&
            mc.player.getMainHandItem().getItem() instanceof ItemScanTool tool)
        {
            if (tool.onTeleport(mc.player, mc.player.getMainHandItem()))
            {
                new ScanToolTeleportMessage().sendToServer();
            }
        }

        Profiler.get().pop();
    }

    public static void onPreClientTickEvent(final Minecraft mc)
    {
        if (mc.player == null || mc.screen != null || mc.level == null) return;

        if (mc.options.keyPickItem.consumeClick())
        {
            BlockPos pos = mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK ? ((BlockHitResult) mc.hitResult).getBlockPos() : null;
            if (pos != null && mc.level.getBlockState(pos).isAir())
            {
                pos = null;
            }
            final ItemStack current = mc.player.getInventory().getSelectedItem();
            if (current.getItem() instanceof ISpecialBlockPickItem clickableItem)
            {
                final boolean ctrlKey = mc.hasControlDown();
                final net.minecraft.world.InteractionResult result = clickableItem.onBlockPick(mc.player, current, pos, ctrlKey);
                if (result instanceof net.minecraft.world.InteractionResult.Pass)
                {
                    ++mc.options.keyPickItem.clickCount;
                }
                else if (!(result instanceof net.minecraft.world.InteractionResult.Fail))
                {
                    new ItemMiddleMouseMessage(pos, ctrlKey).sendToServer();
                }
            }
            else
            {
                ++mc.options.keyPickItem.clickCount;
            }
        }

        for (Iterator<Map.Entry<String, BoxPreviewData>> iterator = RenderingCache.boxRenderingCache.entrySet().iterator(); iterator.hasNext(); )
        {
            final var entry = iterator.next();
            if (entry.getValue().isExpired())
            {
                iterator.remove();
            }
        }
    }

    /**
     * @return true to swallow the scroll (the hotbar doesn't move)
     */
    public static boolean onMouseWheel(final double scrollDeltaX, final double scrollDeltaY)
    {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || mc.level == null) return false;
        if (!mc.player.isShiftKeyDown()) return false;

        final ItemStack current = mc.player.getInventory().getSelectedItem();
        if (current.getItem() instanceof IScrollableItem scrollableItem)
        {
            final boolean ctrlKey = mc.hasControlDown();
            final net.minecraft.world.InteractionResult result = scrollableItem.onMouseScroll(mc.player, current, scrollDeltaX, scrollDeltaY, ctrlKey);
            if (result instanceof net.minecraft.world.InteractionResult.Pass)
            {
                return false;
            }
            if (!(result instanceof net.minecraft.world.InteractionResult.Fail))
            {
                new ItemMiddleMouseMessage(scrollDeltaX, scrollDeltaY, ctrlKey).sendToServer();
            }
            return true;
        }
        return false;
    }

    public static void onDisconnect()
    {
        // clear local caches
        WindowExtendedBuildTool.clearStaticData();
        RenderingCache.clear();
    }
}