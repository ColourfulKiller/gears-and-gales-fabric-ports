package com.ldtteam.fabric.event.entity.living;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class LivingShieldBlockEvent extends LivingEvent implements ICancellableEvent
{
    private final net.minecraft.world.damagesource.DamageSource damageSource;
    private final float blockedDamage;

    public LivingShieldBlockEvent(final net.minecraft.world.entity.Entity entity, final net.minecraft.world.damagesource.DamageSource damageSource, final float blockedDamage)
    {
        super(entity);
        this.damageSource = damageSource;
        this.blockedDamage = blockedDamage;
    }

    public net.minecraft.world.damagesource.DamageSource getDamageSource()
    {
        return damageSource;
    }

    public float getBlockedDamage()
    {
        return blockedDamage;
    }
}
