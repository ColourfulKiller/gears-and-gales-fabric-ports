package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.Loader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.Blocks;

/**
 * Fabric client entrypoint: wires up what NeoForge did through ClientLifecycleSubscriber and ClientEventSubscriber.
 * (NeoForge's ModMismatchEvent has no Fabric counterpart and isn't needed.)
 */
public class BlockUIClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        if (!com.ldtteam.fabric.ModInit.begin("blockui", "client"))
        {
            return;
        }
        com.ldtteam.fabric.event.ClientEventBridge.init();
        // XML window definitions (GUI sprites now live in the vanilla GUI atlas, see AtlasManager)
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(Identifier.fromNamespaceAndPath(BlockUI.MOD_ID, "xml_windows"), Loader.INSTANCE);

        // replace cauldron with plains default color (4159204, with slighty more light in HSL += 8%)
        ColorProviderRegistry.BLOCK.register(
            (state, level, pos, tintIndex) -> level != null && pos != null ? BiomeColors.getAverageWaterColor(level, pos) : 0x638fe9,
            Blocks.WATER_CAULDRON);

        ClientTickEvents.START_CLIENT_TICK.register(ClientEventSubscriber::onClientTickStart);
        ClientTickEvents.END_CLIENT_TICK.register(ClientEventSubscriber::onClientTickEnd);

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            if (client)
            {
                ClientEventSubscriber.onTagsUpdated();
            }
        });

        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, crosshair -> (graphics, tick) -> {
            if (!ClientEventSubscriber.shouldHideCrosshair())
            {
                crosshair.render(graphics, tick);
            }
        });
    }
}
