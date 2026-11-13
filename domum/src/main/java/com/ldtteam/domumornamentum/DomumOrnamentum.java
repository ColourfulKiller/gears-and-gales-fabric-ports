package com.ldtteam.domumornamentum;

import com.ldtteam.domumornamentum.api.DomumOrnamentumAPI;
import com.ldtteam.domumornamentum.block.ModBlocks;
import com.ldtteam.domumornamentum.block.ModCreativeTabs;
import com.ldtteam.domumornamentum.component.ModDataComponents;
import com.ldtteam.domumornamentum.container.ModContainerTypes;
import com.ldtteam.domumornamentum.entity.block.ModBlockEntityTypes;
import com.ldtteam.domumornamentum.network.messages.CreativeSetArchitectCutterSlotMessage;
import com.ldtteam.domumornamentum.recipe.ModRecipeSerializers;
import com.ldtteam.domumornamentum.recipe.ModRecipeTypes;
import com.ldtteam.domumornamentum.util.Constants;
import com.ldtteam.fabric.network.PayloadContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.PacketFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Fabric main entrypoint (was the NeoForge @Mod class plus the mod-bus event handler, which only wired networking and
 * data generation; the generated resources ship in src/main/resources now).
 */
public class DomumOrnamentum implements ModInitializer
{
    public static final Logger LOGGER = LogManager.getLogger(Constants.MOD_ID);

    @Override
    public void onInitialize()
    {
        if (!com.ldtteam.fabric.ModInit.begin("domum_ornamentum", "main"))
        {
            return;
        }
        com.ldtteam.fabric.ModInit.ensure("blockui", "main");
        IDomumOrnamentumApi.Holder.setInstance(DomumOrnamentumAPI.getInstance());

        // data components first: item properties can reference them; blocks before items
        ModDataComponents.REGISTRY.register();
        ModBlocks.BLOCKS.register();
        ModBlocks.ITEMS.register();
        ModBlockEntityTypes.BLOCK_ENTITIES.register();
        ModContainerTypes.CONTAINERS.register();
        ModRecipeTypes.RECIPES.register();
        ModRecipeSerializers.SERIALIZERS.register();
        ModCreativeTabs.TAB_REG.register();

        // 1.21.2+ only sends recipes the client needs; the Architect's Cutter lists its recipes client-side
        net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization.synchronizeRecipeSerializer(ModRecipeSerializers.ARCHITECTS_CUTTER.get());

        PayloadTypeRegistry.playC2S().register(CreativeSetArchitectCutterSlotMessage.ID, CreativeSetArchitectCutterSlotMessage.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(CreativeSetArchitectCutterSlotMessage.ID,
            (message, ctx) -> message.onExecute(PayloadContext.of(ctx.player(), PacketFlow.SERVERBOUND)));
    }
}
