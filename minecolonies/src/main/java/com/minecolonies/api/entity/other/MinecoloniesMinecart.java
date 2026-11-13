package com.minecolonies.api.entity.other;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Special minecolonies minecart that doesn't collide.
 * <p>
 * Fabric 1.21.11 port: rail movement moved into vanilla's MinecartBehavior (1.21.2), so the copied NeoForge rail logic
 * is gone and vanilla's behaviour drives the cart. What stays: no collisions / pushing / interaction, no drops, a
 * lowered seat, and the cart removing itself once it has no passenger.
 */
public class MinecoloniesMinecart extends Minecart
{
    private static final Vec3 LOWERED_PASSENGER_ATTACHMENT = new Vec3(0.0, 0.0, 0.0);

    /**
     * Constructor to create the minecart.
     *
     * @param type  the entity type.
     * @param world the world.
     */
    public MinecoloniesMinecart(final EntityType<?> type, final Level world)
    {
        super(type, world);
    }

    @Override
    protected void destroy(final ServerLevel level, final DamageSource source)
    {
        kill(level);
    }

    @Override
    public InteractionResult interact(final Player player, final InteractionHand hand)
    {
        return InteractionResult.FAIL;
    }

    @Override
    public boolean isPickable()
    {
        return false;
    }

    @Override
    public void push(@NotNull final Entity entityIn)
    {
        // Do nothing
    }

    @Override
    public void playerTouch(final Player entityIn)
    {
        // Do nothing
    }

    @Override
    public boolean isPushable()
    {
        return false;
    }

    @Override
    public boolean canCollideWith(final Entity entity)
    {
        return false;
    }

    @Override
    public void tick()
    {
        super.tick();
        if (!level().isClientSide() && this.tickCount % 20 == 19 && getPassengers().isEmpty())
        {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    @NotNull
    protected Vec3 getPassengerAttachmentPoint(@NotNull final Entity entity, @NotNull final EntityDimensions dimensions, final float scale)
    {
        return LOWERED_PASSENGER_ATTACHMENT;
    }
}
