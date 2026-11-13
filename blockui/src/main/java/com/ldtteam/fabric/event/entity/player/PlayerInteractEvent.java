package com.ldtteam.fabric.event.entity.player;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class PlayerInteractEvent extends PlayerEvent
{
    private final net.minecraft.world.InteractionHand hand;
    private final net.minecraft.core.BlockPos pos;
    private final net.minecraft.core.Direction face;

    public PlayerInteractEvent(final net.minecraft.world.entity.Entity entity, final net.minecraft.world.InteractionHand hand, final net.minecraft.core.BlockPos pos, final net.minecraft.core.Direction face)
    {
        super(entity);
        this.hand = hand;
        this.pos = pos;
        this.face = face;
    }

    public net.minecraft.world.InteractionHand getHand()
    {
        return hand;
    }

    public net.minecraft.core.BlockPos getPos()
    {
        return pos;
    }

    public net.minecraft.core.Direction getFace()
    {
        return face;
    }

    public net.minecraft.world.level.Level getLevel()
    {
        return getEntity().level();
    }

    public net.minecraft.world.item.ItemStack getItemStack()
    {
        return getEntity().getItemInHand(hand);
    }

    public static class RightClickBlock extends PlayerInteractEvent implements ICancellableEvent
    {
        private final net.minecraft.world.phys.BlockHitResult hitVec;

        public RightClickBlock(final net.minecraft.world.entity.Entity entity, final net.minecraft.world.InteractionHand hand, final net.minecraft.core.BlockPos pos, final net.minecraft.core.Direction face, final net.minecraft.world.phys.BlockHitResult hitVec)
        {
            super(entity, hand, pos, face);
            this.hitVec = hitVec;
        }

        public net.minecraft.world.phys.BlockHitResult getHitVec()
        {
            return hitVec;
        }

    }

    public static class RightClickItem extends PlayerInteractEvent implements ICancellableEvent
    {

        public RightClickItem(final net.minecraft.world.entity.Entity entity, final net.minecraft.world.InteractionHand hand, final net.minecraft.core.BlockPos pos, final net.minecraft.core.Direction face)
        {
            super(entity, hand, pos, face);
        }

    private net.minecraft.world.InteractionResult cancellationResult = net.minecraft.world.InteractionResult.PASS;

    public net.minecraft.world.InteractionResult getCancellationResult()
    {
        return cancellationResult;
    }

    public void setCancellationResult(final net.minecraft.world.InteractionResult result)
    {
        this.cancellationResult = result;
    }

    }

    public static class LeftClickBlock extends PlayerInteractEvent implements ICancellableEvent
    {

        public LeftClickBlock(final net.minecraft.world.entity.Entity entity, final net.minecraft.world.InteractionHand hand, final net.minecraft.core.BlockPos pos, final net.minecraft.core.Direction face)
        {
            super(entity, hand, pos, face);
        }

    }

    public static class EntityInteract extends PlayerInteractEvent implements ICancellableEvent
    {
        private final net.minecraft.world.entity.Entity target;

        public EntityInteract(final net.minecraft.world.entity.Entity entity, final net.minecraft.world.InteractionHand hand, final net.minecraft.core.BlockPos pos, final net.minecraft.core.Direction face, final net.minecraft.world.entity.Entity target)
        {
            super(entity, hand, pos, face);
            this.target = target;
        }

        public net.minecraft.world.entity.Entity getTarget()
        {
            return target;
        }

    }

    public static class EntityInteractSpecific extends PlayerInteractEvent implements ICancellableEvent
    {
        private final net.minecraft.world.entity.Entity target;
        private final net.minecraft.world.phys.Vec3 localPos;

        public EntityInteractSpecific(final net.minecraft.world.entity.Entity entity, final net.minecraft.world.InteractionHand hand, final net.minecraft.core.BlockPos pos, final net.minecraft.core.Direction face, final net.minecraft.world.entity.Entity target, final net.minecraft.world.phys.Vec3 localPos)
        {
            super(entity, hand, pos, face);
            this.target = target;
            this.localPos = localPos;
        }

        public net.minecraft.world.entity.Entity getTarget()
        {
            return target;
        }

        public net.minecraft.world.phys.Vec3 getLocalPos()
        {
            return localPos;
        }

    }
}
