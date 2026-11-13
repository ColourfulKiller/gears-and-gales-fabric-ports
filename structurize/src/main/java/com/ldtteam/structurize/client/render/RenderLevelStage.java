package com.ldtteam.structurize.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Replacement for NeoForge's RenderLevelStageEvent, driven by Fabric's WorldRenderEvents (see
 * {@link com.ldtteam.structurize.client.StructurizeClient}). Subscribe to {@link #EVENT}; MineColonies does too.
 * <p>
 * Pose stack positions are camera relative (like NeoForge's). {@link #getSubmitNodeCollector()} is only set during
 * {@link Stage#AFTER_SOLID_BLOCKS}, when vanilla still accepts block entity / entity submissions for this frame;
 * everything else draws immediately.
 */
public record RenderLevelStage(Stage stage,
    PoseStack poseStack,
    Vec3 cameraPosition,
    Frustum frustum,
    DeltaTracker partialTick,
    CameraRenderState cameraRenderState,
    @Nullable SubmitNodeCollector submitNodeCollector)
{
    /**
     * The NeoForge stage names, in frame order. Several fire from the same Fabric event:
     * BEFORE_ENTITIES -> AFTER_SOLID_BLOCKS, AFTER_CUTOUT_BLOCKS;
     * AFTER_ENTITIES -> AFTER_ENTITIES, AFTER_BLOCK_ENTITIES;
     * END_MAIN -> AFTER_TRANSLUCENT_BLOCKS, AFTER_TRIPWIRE_BLOCKS, AFTER_PARTICLES, AFTER_WEATHER, AFTER_LEVEL.
     */
    public enum Stage
    {
        AFTER_SOLID_BLOCKS,
        AFTER_CUTOUT_BLOCKS,
        AFTER_ENTITIES,
        AFTER_BLOCK_ENTITIES,
        AFTER_TRANSLUCENT_BLOCKS,
        AFTER_TRIPWIRE_BLOCKS,
        AFTER_PARTICLES,
        AFTER_WEATHER,
        AFTER_LEVEL
    }

    @FunctionalInterface
    public interface Listener
    {
        void onRenderStage(RenderLevelStage stage);
    }

    public static final Event<Listener> EVENT = EventFactory.createArrayBacked(Listener.class, listeners -> stage -> {
        for (final Listener listener : listeners)
        {
            listener.onRenderStage(stage);
        }
    });

    public Stage getStage()
    {
        return stage;
    }

    public PoseStack getPoseStack()
    {
        return poseStack;
    }

    public Frustum getFrustum()
    {
        return frustum;
    }

    public DeltaTracker getPartialTick()
    {
        return partialTick;
    }

    public Vec3 getCameraPosition()
    {
        return cameraPosition;
    }

    @Nullable
    public SubmitNodeCollector getSubmitNodeCollector()
    {
        return submitNodeCollector;
    }
}
