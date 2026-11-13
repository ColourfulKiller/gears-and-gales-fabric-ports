package com.ldtteam.structurize.client;

import com.ldtteam.common.language.LanguageHandler;
import com.ldtteam.structurize.api.Log;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.blockentities.ModBlockEntities;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.client.render.RenderLevelStage;
import com.ldtteam.structurize.client.render.RenderLevelStage.Stage;
import com.ldtteam.structurize.event.ClientEventSubscriber;
import com.ldtteam.structurize.event.LifecycleSubscriber;
import com.ldtteam.structurize.items.ItemStackTooltip;
import com.ldtteam.structurize.storage.ClientFutureProcessor;
import com.ldtteam.structurize.storage.ClientStructurePackLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

/**
 * Fabric client entrypoint: what the NeoForge client mod-bus subscriber (ClientLifecycleSubscriber) and the game-bus
 * ClientEventSubscriber registrations did.
 */
public class StructurizeClient implements ClientModInitializer
{
    /** Per-frame state captured during extraction, used by the drawing events of the same frame. */
    private static WorldExtractionContext extraction;

    @Override
    public void onInitializeClient()
    {
        if (!com.ldtteam.fabric.ModInit.begin("structurize", "client"))
        {
            return;
        }
        com.ldtteam.fabric.ModInit.ensure("blockui", "client");
        com.ldtteam.fabric.ModInit.ensure("domum_ornamentum", "client");
        LifecycleSubscriber.onClientNetworkRegistry();
        ClientStructurePackLoader.onClientLoading();
        ClientLifecycleEvents.CLIENT_STARTED.register(mc -> LanguageHandler.setMClanguageLoaded());

        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(Constants.resLocStruct("blueprint_renderer_cache"),
            (ResourceManagerReloadListener) manager -> {
                Log.getLogger().debug("Clearing blueprint renderer cache.");
                BlueprintHandler.getInstance().clearCache();
            });

        BlockRenderLayerMap.putBlock(ModBlocks.blockSubstitution.get(), ChunkSectionLayer.TRANSLUCENT);
        BlockEntityRenderers.register(ModBlockEntities.TAG_SUBSTITUTION.get(), TagSubstitutionRenderer::new);
        SpecialModelRenderers.ID_MAPPER.put(Constants.resLocStruct("tag_substitution"), TagSubstitutionRenderer.ItemRenderer.Unbaked.MAP_CODEC);
        TooltipComponentCallback.EVENT.register(data -> data instanceof final ItemStackTooltip tooltip ? new ClientItemStackTooltip(tooltip) : null);
        ModKeyMappings.register();

        ClientTickEvents.START_CLIENT_TICK.register(ClientEventSubscriber::onPreClientTickEvent);
        ClientTickEvents.START_CLIENT_TICK.register(ClientStructurePackLoader::onWorldTick);
        ClientTickEvents.END_CLIENT_TICK.register(ClientEventSubscriber::onClientTickEvent);
        ClientTickEvents.END_CLIENT_TICK.register(ClientFutureProcessor::onWorldTick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, mc) -> ClientEventSubscriber.onDisconnect());

        HudElementRegistry.replaceElement(VanillaHudElements.HEALTH_BAR, element -> (graphics, tick) -> {
            if (!ClientEventSubscriber.shouldHideSurvivalBars()) element.render(graphics, tick);
        });
        HudElementRegistry.replaceElement(VanillaHudElements.FOOD_BAR, element -> (graphics, tick) -> {
            if (!ClientEventSubscriber.shouldHideSurvivalBars()) element.render(graphics, tick);
        });

        // world rendering: Fabric's events drive the NeoForge-style stages (see RenderLevelStage)
        RenderLevelStage.EVENT.register(com.ldtteam.structurize.event.WorldRenderContext.INSTANCE::renderWorldLastEvent);
        WorldRenderEvents.END_EXTRACTION.register(context -> extraction = context);
        WorldRenderEvents.BEFORE_ENTITIES.register(context -> fire(context, true, Stage.AFTER_SOLID_BLOCKS, Stage.AFTER_CUTOUT_BLOCKS));
        WorldRenderEvents.AFTER_ENTITIES.register(context -> fire(context, false, Stage.AFTER_ENTITIES, Stage.AFTER_BLOCK_ENTITIES));
        WorldRenderEvents.END_MAIN.register(context -> fire(context, false,
            Stage.AFTER_TRANSLUCENT_BLOCKS, Stage.AFTER_TRIPWIRE_BLOCKS, Stage.AFTER_PARTICLES, Stage.AFTER_WEATHER, Stage.AFTER_LEVEL));
    }

    private static void fire(final net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext context, final boolean acceptsSubmissions, final Stage... stages)
    {
        if (extraction == null || Minecraft.getInstance().level == null)
        {
            return;
        }
        for (final Stage stage : stages)
        {
            RenderLevelStage.EVENT.invoker().onRenderStage(new RenderLevelStage(stage,
                context.matrices(),
                context.worldState().cameraRenderState.pos,
                extraction.frustum(),
                extraction.tickCounter(),
                context.worldState().cameraRenderState,
                acceptsSubmissions ? context.commandQueue() : null));
        }
    }
}