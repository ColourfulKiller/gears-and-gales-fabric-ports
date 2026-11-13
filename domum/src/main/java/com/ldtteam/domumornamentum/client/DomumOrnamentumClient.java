package com.ldtteam.domumornamentum.client;

import com.ldtteam.domumornamentum.block.IModBlocks;
import com.ldtteam.domumornamentum.block.ModBlocks;
import com.ldtteam.domumornamentum.block.decorative.ExtraBlock;
import com.ldtteam.domumornamentum.client.color.MateriallyTexturedBlockBlockColor;
import com.ldtteam.domumornamentum.client.event.handlers.ClientTickEventHandler;
import com.ldtteam.domumornamentum.client.screens.ArchitectsCutterScreen;
import com.ldtteam.domumornamentum.container.ModContainerTypes;
import com.ldtteam.domumornamentum.shingles.ShingleHeightType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.block.Block;

/**
 * Fabric client entrypoint: what NeoForge did in the client mod-bus handler (menu screen, render layers), the colour
 * handler registration and the client tick handler.
 * <p>
 * Door / trapdoor / panel / post item variants used ItemProperties overrides on NeoForge; on 1.21.4+ they are a
 * "select" on the block_state property inside the generated item model definitions (assets/.../items/*.json).
 */
public class DomumOrnamentumClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        if (!com.ldtteam.fabric.ModInit.begin("domum_ornamentum", "client"))
        {
            return;
        }
        com.ldtteam.fabric.ModInit.ensure("blockui", "client");
        MenuScreens.register(ModContainerTypes.ARCHITECTS_CUTTER.get(), ArchitectsCutterScreen::new);

        final IModBlocks blocks = IModBlocks.getInstance();
        cutout(blocks.getArchitectsCutter(), blocks.getStandingBarrel(), blocks.getLayingBarrel());
        translucent(blocks.getShingleSlab(), blocks.getPaperWall(), blocks.getFence(), blocks.getFenceGate(), blocks.getSlab(),
            blocks.getStair(), blocks.getWall(), blocks.getFancyDoor(), blocks.getFancyTrapdoor(), blocks.getTrapdoor(), blocks.getDoor(),
            blocks.getPanel(), blocks.getPost(), blocks.getTiledPaperWall(), blocks.getDynamicTimberFrame());
        for (final ShingleHeightType heightType : ShingleHeightType.values())
        {
            translucent(blocks.getShingle(heightType));
        }
        blocks.getFloatingCarpets().forEach(DomumOrnamentumClient::cutout);
        blocks.getTimberFrames().forEach(DomumOrnamentumClient::translucent);
        blocks.getExtraTopBlocks().forEach(b -> {
            if (((ExtraBlock) b).getType().isTranslucent())
            {
                translucent(b);
            }
        });

        ColorProviderRegistry.BLOCK.register(new MateriallyTexturedBlockBlockColor(), ModBlocks.getMateriallyTexturableBlocks());

        // retexturing (was the NeoForge domum_ornamentum:materially_textured geometry loader + baked model)
        net.minecraft.client.renderer.item.ItemModels.ID_MAPPER.put(com.ldtteam.domumornamentum.util.Constants.resLocDO("materially_textured"),
            com.ldtteam.domumornamentum.client.model.MateriallyTexturedItemModel.Unbaked.MAP_CODEC);
        net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.register(pluginContext -> {
            final com.ldtteam.domumornamentum.client.model.Retexturer retexturer = com.ldtteam.domumornamentum.client.model.Retexturer.reset();
            pluginContext.modifyBlockModelAfterBake().register((model, context) ->
                context.state().getBlock() instanceof com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock
                    ? new com.ldtteam.domumornamentum.client.model.MateriallyTexturedBlockStateModel(model, retexturer)
                    : model);
        });

        ClientTickEvents.START_CLIENT_TICK.register(mc -> ClientTickEventHandler.getInstance().onClientTick());
    }

    private static void cutout(final Block... blocks)
    {
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT, blocks);
    }

    private static void translucent(final Block... blocks)
    {
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.TRANSLUCENT, blocks);
    }
}
