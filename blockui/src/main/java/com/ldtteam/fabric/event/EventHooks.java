package com.ldtteam.fabric.event;

import com.ldtteam.fabric.event.entity.living.LivingConversionEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Consumer;

/** The parts of NeoForge's EventHooks the LDT mods call. */
public final class EventHooks
{
    private EventHooks()
    {
    }

    /** Whether a conversion may happen (fires LivingConversionEvent.Pre). */
    public static boolean canLivingConvert(final LivingEntity entity, final EntityType<? extends LivingEntity> outcome, final Consumer<Integer> timer)
    {
        return !NeoForge.EVENT_BUS.post(new LivingConversionEvent.Pre(entity, outcome)).isCanceled();
    }
}