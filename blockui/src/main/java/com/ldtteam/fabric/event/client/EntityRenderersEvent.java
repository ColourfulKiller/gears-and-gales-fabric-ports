package com.ldtteam.fabric.event.client;

import com.ldtteam.fabric.event.bus.Event;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

/** NeoForge's EntityRenderersEvent family: straight to Fabric's / vanilla's renderer registries. */
public abstract class EntityRenderersEvent extends Event
{
    public static class RegisterLayerDefinitions extends EntityRenderersEvent
    {
        public void registerLayerDefinition(final ModelLayerLocation layer, final Supplier<LayerDefinition> supplier)
        {
            EntityModelLayerRegistry.registerModelLayer(layer, supplier::get);
        }
    }

    public static class RegisterRenderers extends EntityRenderersEvent
    {
        public <T extends Entity> void registerEntityRenderer(final EntityType<? extends T> type, final EntityRendererProvider<T> provider)
        {
            EntityRendererRegistry.register(type, provider);
        }

        public <T extends BlockEntity, S extends BlockEntityRenderState> void registerBlockEntityRenderer(final BlockEntityType<? extends T> type,
            final BlockEntityRendererProvider<T, S> provider)
        {
            BlockEntityRenderers.register(type, provider);
        }
    }
}