package com.ldtteam.fabric.event.entity.living;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class LivingIncomingDamageEvent extends LivingEvent implements ICancellableEvent
{
    private final net.minecraft.world.damagesource.DamageSource source;

    public LivingIncomingDamageEvent(final net.minecraft.world.entity.Entity entity, final net.minecraft.world.damagesource.DamageSource source)
    {
        super(entity);
        this.source = source;
    }

    public net.minecraft.world.damagesource.DamageSource getSource()
    {
        return source;
    }

    private float amount;

    public float getAmount()
    {
        return amount;
    }

    public void setAmount(final float amount)
    {
        this.amount = amount;
    }
}
