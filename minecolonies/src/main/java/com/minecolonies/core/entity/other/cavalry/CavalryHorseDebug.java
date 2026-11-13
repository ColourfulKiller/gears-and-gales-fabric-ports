package com.minecolonies.core.entity.other.cavalry;

import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;

import com.ldtteam.fabric.event.bus.SubscribeEvent;

import com.ldtteam.fabric.event.entity.EntityLeaveLevelEvent;
import com.ldtteam.fabric.event.entity.living.LivingDeathEvent;
import com.ldtteam.fabric.event.entity.living.LivingIncomingDamageEvent;

public class CavalryHorseDebug
{
    @SubscribeEvent
    public static void onIncomingDamage(final LivingIncomingDamageEvent e)
    {
        if (e.getEntity() instanceof CavalryHorseEntity)
        {
            Log.getLogger().info(
                "CavHorse incoming damage: {} cause={} amount={}",
                e.getEntity().getUUID(),
                e.getSource().type().msgId(),
                e.getAmount(),
                new Exception("Damage event stack trace")
            );
        }
    }

    @SubscribeEvent
    public static void onDeath(final LivingDeathEvent e)
    {
        if (e.getEntity() instanceof CavalryHorseEntity)
        {
            Log.getLogger().warn(
                "CavHorse died: {} cause={}",
                e.getEntity().getUUID(),
                e.getSource().type().msgId(),
                new Exception("Death event stack trace")
            );
        }
    }

    @SubscribeEvent
    public static void onLeave(final EntityLeaveLevelEvent e)
    {
        if (e.getEntity() instanceof CavalryHorseEntity ch)
        {
            Log.getLogger().warn(
                "CavHorse left level: {} reason={}",
                ch.getUUID(),
                ch.getRemovalReason(),
                new Exception("EntityLeaveLevelEvent stack trace")
            );
        }
    }
}
