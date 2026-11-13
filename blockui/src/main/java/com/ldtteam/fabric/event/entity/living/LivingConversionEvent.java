package com.ldtteam.fabric.event.entity.living;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class LivingConversionEvent extends LivingEvent
{

    public LivingConversionEvent(final net.minecraft.world.entity.Entity entity)
    {
        super(entity);
    }

    public static class Pre extends LivingConversionEvent implements ICancellableEvent
    {
        private final net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.LivingEntity> outcome;

        public Pre(final net.minecraft.world.entity.Entity entity, final net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.LivingEntity> outcome)
        {
            super(entity);
            this.outcome = outcome;
        }

        public net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.LivingEntity> getOutcome()
        {
            return outcome;
        }

    }
}
