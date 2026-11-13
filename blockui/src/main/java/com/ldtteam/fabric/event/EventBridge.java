package com.ldtteam.fabric.event;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.entity.EntityJoinLevelEvent;
import com.ldtteam.fabric.event.entity.EntityLeaveLevelEvent;
import com.ldtteam.fabric.event.entity.living.LivingDamageEvent;
import com.ldtteam.fabric.event.entity.living.LivingDeathEvent;
import com.ldtteam.fabric.event.entity.living.LivingIncomingDamageEvent;
import com.ldtteam.fabric.event.entity.player.AttackEntityEvent;
import com.ldtteam.fabric.event.entity.player.PlayerEvent;
import com.ldtteam.fabric.event.entity.player.PlayerInteractEvent;
import com.ldtteam.fabric.event.level.BlockEvent;
import com.ldtteam.fabric.event.level.ChunkEvent;
import com.ldtteam.fabric.event.level.LevelEvent;
import com.ldtteam.fabric.event.server.ServerAboutToStartEvent;
import com.ldtteam.fabric.event.server.ServerStartedEvent;
import com.ldtteam.fabric.event.server.ServerStartingEvent;
import com.ldtteam.fabric.event.server.ServerStoppedEvent;
import com.ldtteam.fabric.event.server.ServerStoppingEvent;
import com.ldtteam.fabric.event.tick.LevelTickEvent;
import com.ldtteam.fabric.event.tick.ServerTickEvent;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Fires the NeoForge-shaped events of com.ldtteam.fabric.event into {@link NeoForge#EVENT_BUS}: from Fabric API
 * callbacks here, and from the mixins in com.ldtteam.fabric.event.mixin for events Fabric has no callback for.
 * Server-side (common) events only; client ones are in {@link ClientEventBridge}.
 */
public final class EventBridge
{
    private static boolean initialized;

    /** Entities whose join event already fired from ServerLevel#addEntity (so ENTITY_LOAD doesn't fire it again). */
    private static final Set<Entity> ANNOUNCED = Collections.newSetFromMap(Collections.synchronizedMap(new WeakHashMap<>()));

    private EventBridge()
    {
    }

    public static <T extends Event> T post(final T event)
    {
        return NeoForge.EVENT_BUS.post(event);
    }

    public static boolean wants(final Class<? extends Event> type)
    {
        return NeoForge.EVENT_BUS.hasListeners(type);
    }

    public static synchronized void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        // --- lifecycle / ticks
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            post(new ServerAboutToStartEvent(server));
            post(new ServerStartingEvent(server));
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> post(new ServerStartedEvent(server)));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> post(new ServerStoppingEvent(server)));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> post(new ServerStoppedEvent(server)));
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) ->
            post(new OnDatapackSyncEvent(player.level().getServer().getPlayerList(), player)));
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> post(new TagsUpdatedEvent(registries, client)));
        ServerTickEvents.START_SERVER_TICK.register(server -> post(new ServerTickEvent.Pre(server)));
        ServerTickEvents.END_SERVER_TICK.register(server -> post(new ServerTickEvent.Post(server)));
        ServerTickEvents.START_WORLD_TICK.register(level -> post(new LevelTickEvent.Pre(level)));
        ServerTickEvents.END_WORLD_TICK.register(level -> post(new LevelTickEvent.Post(level)));
        ServerWorldEvents.LOAD.register((server, level) -> post(new LevelEvent.Load(level)));
        ServerWorldEvents.UNLOAD.register((server, level) -> post(new LevelEvent.Unload(level)));
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk) -> post(new ChunkEvent.Load(level, chunk, false)));
        ServerChunkEvents.CHUNK_GENERATE.register((level, chunk) -> post(new ChunkEvent.Load(level, chunk, true)));
        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> post(new ChunkEvent.Unload(level, chunk)));
        LootTableEvents.MODIFY.register((key, builder, source, registries) ->
            post(new LootTableLoadEvent(key.identifier(), new LootTableLoadEvent.TableAccess(builder))));

        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
            post(new RegisterCommandsEvent(dispatcher, selection, buildContext)));

        // --- players
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> post(new PlayerEvent.PlayerLoggedInEvent(handler.player)));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> post(new PlayerEvent.PlayerLoggedOutEvent(handler.player)));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) ->
            post(new PlayerEvent.PlayerChangedDimensionEvent(player, origin.dimension(), destination.dimension())));

        // --- entities
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (ANNOUNCED.remove(entity) || !wants(EntityJoinLevelEvent.class))
            {
                return;
            }
            // loaded from disk: can't be refused any more, so a cancel discards it (what NeoForge's cancel amounts to)
            if (post(new EntityJoinLevelEvent(entity, level, true)).isCanceled())
            {
                entity.discard();
            }
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> post(new EntityLeaveLevelEvent(entity, level)));
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> post(new LivingDeathEvent(entity, source)));
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            final LivingIncomingDamageEvent incoming = new LivingIncomingDamageEvent(entity, source);
            incoming.setAmount(amount);
            if (post(incoming).isCanceled() || incoming.getAmount() <= 0)
            {
                return false;
            }
            // NeoForge could lower the damage here; Fabric can only allow or deny, so "no damage" denies
            return !(post(new LivingDamageEvent.Pre(entity, source, amount)).getNewDamage() <= 0);
        });

        // --- interaction (both sides, like NeoForge's PlayerInteractEvent)
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) ->
            !post(new BlockEvent.BreakEvent(level, pos, state, player)).isCanceled());
        UseBlockCallback.EVENT.register((player, level, hand, hit) ->
            post(new PlayerInteractEvent.RightClickBlock(player, hand, hit.getBlockPos(), hit.getDirection(), hit)).isCanceled()
                ? InteractionResult.FAIL : InteractionResult.PASS);
        UseItemCallback.EVENT.register((player, level, hand) -> {
            final PlayerInteractEvent.RightClickItem event = post(new PlayerInteractEvent.RightClickItem(player, hand, player.blockPosition(), null));
            if (!event.isCanceled())
            {
                return InteractionResult.PASS;
            }
            // PASS would let vanilla carry on; a cancel must stop the use
            return event.getCancellationResult() instanceof InteractionResult.Pass ? InteractionResult.FAIL : event.getCancellationResult();
        });
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) ->
            post(new PlayerInteractEvent.LeftClickBlock(player, hand, pos, direction)).isCanceled() ? InteractionResult.FAIL : InteractionResult.PASS);
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            final PlayerInteractEvent event = hit == null
                ? new PlayerInteractEvent.EntityInteract(player, hand, entity.blockPosition(), null, entity)
                : new PlayerInteractEvent.EntityInteractSpecific(player, hand, entity.blockPosition(), null, entity, hit.getLocation().subtract(entity.position()));
            return post(event).isCanceled() ? InteractionResult.FAIL : InteractionResult.PASS;
        });
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) ->
            post(new AttackEntityEvent(player, entity)).isCanceled() ? InteractionResult.FAIL : InteractionResult.PASS);
    }

    /** From ServerLevel#addEntity: a fresh entity joining. @return false to refuse it */
    public static boolean onEntityAdded(final Entity entity, final net.minecraft.server.level.ServerLevel level)
    {
        if (!wants(EntityJoinLevelEvent.class))
        {
            return true;
        }
        ANNOUNCED.add(entity);
        if (post(new EntityJoinLevelEvent(entity, level, false)).isCanceled())
        {
            ANNOUNCED.remove(entity);
            return false;
        }
        return true;
    }
}
