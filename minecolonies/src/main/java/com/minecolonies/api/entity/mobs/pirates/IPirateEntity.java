package com.minecolonies.api.entity.mobs.pirates;

import com.minecolonies.api.util.IItemHandlerCapProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import com.ldtteam.fabric.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public interface IPirateEntity extends Enemy, IItemHandlerCapProvider
{
    @Override
    @Nullable
    default IItemHandler getItemHandlerCap(final Direction direction)
    {
        // LivingEntities have cap registered by forge
        return com.ldtteam.fabric.items.ItemHandlers.forEntity((LivingEntity) this, null);
    }
}
