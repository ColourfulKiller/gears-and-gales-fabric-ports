package com.ldtteam.fabric;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.FuelValues;
import org.jetbrains.annotations.Nullable;

/** 1.21.2 moved furnace fuel into data-driven FuelValues owned by the server / level. */
public final class FuelHooks
{
    private FuelHooks()
    {
    }

    /** The fuel table of the running server, else of the client's level; null outside a world. */
    public static @Nullable FuelValues fuelValues()
    {
        final MinecraftServer server = com.ldtteam.fabric.network.Networking.server();
        if (server != null)
        {
            return server.fuelValues();
        }
        if (Env.isClient())
        {
            final Level level = ClientEnv.level();
            if (level != null)
            {
                return level.fuelValues();
            }
        }
        return null;
    }

    /** Was {@code FurnaceBlockEntity.isFuel(stack)}. */
    public static boolean isFuel(final ItemStack stack)
    {
        final FuelValues values = fuelValues();
        return values != null && values.isFuel(stack);
    }

    /** Was {@code stack.getBurnTime(type)} / {@code AbstractFurnaceBlockEntity.getFuel().get(item)}. */
    public static int burnDuration(final ItemStack stack)
    {
        final FuelValues values = fuelValues();
        return values == null ? 0 : values.burnDuration(stack);
    }
}