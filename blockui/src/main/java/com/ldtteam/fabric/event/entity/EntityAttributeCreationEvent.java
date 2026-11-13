package com.ldtteam.fabric.event.entity;

import com.ldtteam.fabric.event.bus.Event;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

/** NeoForge's EntityAttributeCreationEvent: straight to Fabric's default attribute registry. */
public class EntityAttributeCreationEvent extends Event
{
    public void put(final EntityType<? extends LivingEntity> type, final AttributeSupplier attributes)
    {
        FabricDefaultAttributeRegistry.register(type, attributes);
    }
}