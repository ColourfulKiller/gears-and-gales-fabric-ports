package com.minecolonies.api.compatibility;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Optional Trinkets support (the Fabric counterpart of Curios): the build goggles can be worn in the trinkets
 * "head/face" slot (data/trinkets/entities + tags). Reached through reflection so MineColonies has no hard
 * dependency on Trinkets / Cardinal Components.
 */
public final class TrinketsCompat
{
    private static final boolean PRESENT = FabricLoader.getInstance().isModLoaded("trinkets");
    private static Method getComponent;
    private static Method isEquipped;
    private static boolean failed;

    private TrinketsCompat()
    {
    }

    /** Whether the entity has the item in any Trinkets slot (false without Trinkets). */
    public static boolean isEquipped(final LivingEntity entity, final Item item)
    {
        if (!PRESENT || failed || entity == null)
        {
            return false;
        }
        try
        {
            if (getComponent == null)
            {
                final Class<?> api = Class.forName("dev.emi.trinkets.api.TrinketsApi");
                getComponent = api.getMethod("getTrinketComponent", LivingEntity.class);
                isEquipped = Class.forName("dev.emi.trinkets.api.TrinketComponent").getMethod("isEquipped", Item.class);
            }
            final Optional<?> component = (Optional<?>) getComponent.invoke(null, entity);
            return component.isPresent() && (boolean) isEquipped.invoke(component.get(), item);
        }
        catch (final ReflectiveOperationException | RuntimeException e)
        {
            failed = true;
            com.minecolonies.api.util.Log.getLogger().warn("Trinkets compat disabled", e);
            return false;
        }
    }
}