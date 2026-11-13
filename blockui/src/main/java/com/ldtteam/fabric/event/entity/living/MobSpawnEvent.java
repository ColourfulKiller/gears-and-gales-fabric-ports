package com.ldtteam.fabric.event.entity.living;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class MobSpawnEvent extends LivingEvent
{
    private final net.minecraft.world.level.ServerLevelAccessor level;
    private final double x;
    private final double y;
    private final double z;

    public MobSpawnEvent(final net.minecraft.world.entity.Entity entity, final net.minecraft.world.level.ServerLevelAccessor level, final double x, final double y, final double z)
    {
        super(entity);
        this.level = level;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public net.minecraft.world.level.ServerLevelAccessor getLevel()
    {
        return level;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }

    @Override
    public net.minecraft.world.entity.Mob getEntity()
    {
        return (net.minecraft.world.entity.Mob) super.getEntity();
    }

    public static class PositionCheck extends MobSpawnEvent
    {
        private final net.minecraft.world.entity.EntitySpawnReason spawnType;

        public PositionCheck(final net.minecraft.world.entity.Entity entity, final net.minecraft.world.level.ServerLevelAccessor level, final double x, final double y, final double z, final net.minecraft.world.entity.EntitySpawnReason spawnType)
        {
            super(entity, level, x, y, z);
            this.spawnType = spawnType;
        }

        public net.minecraft.world.entity.EntitySpawnReason getSpawnType()
        {
            return spawnType;
        }

    public enum Result
    {
        SUCCEED, DEFAULT, FAIL
    }

    private Result result = Result.DEFAULT;

    public void setResult(final Result result)
    {
        this.result = result;
    }

    public Result getResult()
    {
        return result;
    }

    }
}
