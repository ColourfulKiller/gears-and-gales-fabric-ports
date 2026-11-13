package com.minecolonies.core.commands.arguments;

import com.minecolonies.core.commands.colonycommands.CommandDeleteColony;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.Identifier;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * This class handles registration for custom argument types (Fabric: through ArgumentTypeRegistry, which also maps the class).
 */
public class ModArgumentTypes
{
    private static boolean registered = false;

    public static void init()
    {
        if (registered)
        {
            return;
        }
        registered = true;
        ArgumentTypeRegistry.registerArgumentType(Identifier.fromNamespaceAndPath(MOD_ID, "colony_id"), ColonyIdArgument.class, SingletonArgumentInfo.contextFree(ColonyIdArgument::id));
        ArgumentTypeRegistry.registerArgumentType(Identifier.fromNamespaceAndPath(MOD_ID, "multi_colony_id"), MultiColonyIdArgument.class, SingletonArgumentInfo.contextFree(MultiColonyIdArgument::id));
        ArgumentTypeRegistry.registerArgumentType(Identifier.fromNamespaceAndPath(MOD_ID, "delete_buildings"), CommandDeleteColony.DeleteBuildingsArgumentType.class,
          SingletonArgumentInfo.contextFree(CommandDeleteColony.DeleteBuildingsArgumentType::argument));
    }
}