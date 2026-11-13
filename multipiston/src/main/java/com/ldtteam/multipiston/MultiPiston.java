package com.ldtteam.multipiston;

import com.ldtteam.multipiston.network.MultiPistonChangeMessage;
import com.ldtteam.fabric.registry.DeferredRegister;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

import static com.ldtteam.multipiston.ModBlocks.BLOCKS;
import static com.ldtteam.multipiston.ModBlocks.ITEMS;
import static com.ldtteam.multipiston.ModTileEntities.TILE_ENTITIES;

/**
 * Fabric main entrypoint (was the NeoForge @Mod constructor + mod-bus network registration).
 */
public class MultiPiston implements ModInitializer
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String                            MOD_ID  = "multipiston";
    public static final DeferredRegister<CreativeModeTab> TAB_REG = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final Supplier<CreativeModeTab> GENERAL = TAB_REG.register("general", () -> net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup.builder()
        .icon(() -> new ItemStack(ModBlocks.multipiston.get()))
        .title(Component.translatable("block.multipiston.multipistonblock"))
        .displayItems((config, output) -> output.accept(ModBlocks.multipiston.get()))
        .build());

    @Override
    public void onInitialize()
    {
        if (!com.ldtteam.fabric.ModInit.begin("multipiston", "main"))
        {
            return;
        }
        com.ldtteam.fabric.ModInit.ensure("blockui", "main");
        BLOCKS.register();
        ITEMS.register();
        TILE_ENTITIES.register();
        TAB_REG.register();

        MultiPistonChangeMessage.TYPE.register();
    }
}