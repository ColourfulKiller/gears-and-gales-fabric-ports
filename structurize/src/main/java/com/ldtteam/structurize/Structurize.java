package com.ldtteam.structurize;

import com.ldtteam.common.config.Configurations;
import com.ldtteam.common.language.LanguageHandler;
import com.ldtteam.structurize.api.Log;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.blockentities.ModBlockEntities;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blueprints.v1.DataFixerUtils;
import com.ldtteam.structurize.blueprints.v1.DataVersion;
import com.ldtteam.structurize.component.ModDataComponents;
import com.ldtteam.structurize.config.ClientConfiguration;
import com.ldtteam.structurize.config.ServerConfiguration;
import com.ldtteam.structurize.event.EventSubscriber;
import com.ldtteam.structurize.event.LifecycleSubscriber;
import com.ldtteam.structurize.items.ModItemGroups;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.fabric.Env;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.datafix.DataFixers;

/**
 * Fabric main entrypoint (was the NeoForge @Mod constructor). Event wiring lives in {@link EventSubscriber} /
 * {@link LifecycleSubscriber}; the client side in {@link com.ldtteam.structurize.client.StructurizeClient}.
 */
public class Structurize implements ModInitializer
{
    private static Configurations<ClientConfiguration, ServerConfiguration, ?> config;

    @Override
    public void onInitialize()
    {
        if (!com.ldtteam.fabric.ModInit.begin("structurize", "main"))
        {
            return;
        }
        com.ldtteam.fabric.ModInit.ensure("blockui", "main");
        com.ldtteam.fabric.ModInit.ensure("domum_ornamentum", "main");
        LanguageHandler.loadLangPath("assets/structurize/lang/%s.json");
        config = new Configurations<>(Constants.MOD_ID, ClientConfiguration::new, ServerConfiguration::new, null);

        ModDataComponents.REGISTRY.register();
        ModBlocks.BLOCKS.register();
        ModItems.ITEMS.register();
        ModBlockEntities.BLOCK_ENTITIES.register();
        ModItemGroups.TAB_REG.register();

        LifecycleSubscriber.onNetworkRegistry();
        EventSubscriber.register();

        if (DataFixerUtils.isVanillaDF)
        {
            if ((DataFixers.getDataFixer().getSchema(Integer.MAX_VALUE - 1).getVersionKey()) >= DataVersion.UPCOMING.getDataVersion() * 10)
            {
                throw new RuntimeException("You are trying to run old mod on much newer vanilla. Missing some newest data versions. Please update com/ldtteam/structures/blueprints/v1/DataVersion");
            }
            else if (!Env.PRODUCTION && DataVersion.CURRENT == DataVersion.UPCOMING)
            {
                throw new RuntimeException("Missing some newest data versions. Please update src/main/java/com/ldtteam/structurize/blueprints/v1/DataVersion.java");
            }
        }
        else
        {
            Log.getLogger().error("----------------------------------------------------------------- \n "
                                    + "Invalid DataFixer detected, schematics might not paste correctly! \n"
                                    +  "The following DataFixer was added: " + DataFixers.getDataFixer().getClass() + "\n"
                                    + "-----------------------------------------------------------------");
        }
    }

    public static Configurations<ClientConfiguration, ServerConfiguration, ?> getConfig()
    {
        return config;
    }
}