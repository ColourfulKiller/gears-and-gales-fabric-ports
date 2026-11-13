package com.ldtteam.fabric.event.entity.living;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class LivingDamageEvent extends LivingEvent
{
    private final net.minecraft.world.damagesource.DamageSource source;

    public LivingDamageEvent(final net.minecraft.world.entity.Entity entity, final net.minecraft.world.damagesource.DamageSource source)
    {
        super(entity);
        this.source = source;
    }

    public net.minecraft.world.damagesource.DamageSource getSource()
    {
        return source;
    }

    public static class Pre extends LivingDamageEvent
    {
        private final float originalDamage;

        public Pre(final net.minecraft.world.entity.Entity entity, final net.minecraft.world.damagesource.DamageSource source, final float originalDamage)
        {
            super(entity, source);
            this.originalDamage = originalDamage;
        }

        public float getOriginalDamage()
        {
            return originalDamage;
        }

    private float newDamage = Float.NaN;

    public float getNewDamage()
    {
        return Float.isNaN(newDamage) ? getOriginalDamage() : newDamage;
    }

    public void setNewDamage(final float newDamage)
    {
        this.newDamage = newDamage;
    }

    }
}
