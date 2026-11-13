package com.ldtteam.fabric.nbt;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import com.ldtteam.fabric.ClientEnv;
import com.ldtteam.fabric.Env;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 1.21.6 moved entity save/load from CompoundTag to ValueOutput / ValueInput. These keep the CompoundTag-based call
 * sites of the 1.21.1 LDT code working (problems are discarded, like the old direct NBT code did).
 */
public final class NbtIO
{
    private NbtIO()
    {
    }

    /**
     * Was {@code entity.save(tag)}: writes the entity (with its id) into {@code target}.
     *
     * @return false when the entity can't be saved on its own (removed, or a passenger), as before
     */
    public static boolean saveEntity(final Entity entity, final CompoundTag target)
    {
        final TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());
        if (!entity.save(output))
        {
            return false;
        }
        target.merge(output.buildResult());
        return true;
    }

    /** Was {@code entity.saveWithoutId(tag)}. */
    public static CompoundTag saveEntityWithoutId(final Entity entity)
    {
        final TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());
        entity.saveWithoutId(output);
        return output.buildResult();
    }

    /** Was {@code entity.load(tag)}. */
    public static void loadEntity(final Entity entity, final CompoundTag tag)
    {
        entity.load(TagValueInput.create(ProblemReporter.DISCARDING, entity.registryAccess(), tag));
    }

    /** Was {@code EntityType.by(tag)}. */
    public static Optional<EntityType<?>> entityType(final CompoundTag tag, final HolderLookup.Provider registries)
    {
        return EntityType.by(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
    }

    /**
     * The CompoundTag behind a tag-backed ValueInput (which is what block entities and entities get when loaded from
     * NBT: chunks, blueprints, packets), so 1.21.1-style {@code loadAdditional(CompoundTag, Provider)} bodies keep
     * working unchanged. Other inputs (e.g. the shared empty one) yield an empty tag.
     */
    public static CompoundTag tag(final ValueInput input)
    {
        return input instanceof final TagValueInput tagInput ? tagInput.input : new CompoundTag();
    }

    /**
     * The CompoundTag a tag-backed ValueOutput writes into; keys put here end up in the saved data. Other outputs get
     * a detached tag (writes are lost), which vanilla never hands to block entities.
     */
    public static CompoundTag tag(final ValueOutput output)
    {
        return output instanceof final TagValueOutput tagOutput ? tagOutput.output : new CompoundTag();
    }

    /**
     * Registries for save code that took a HolderLookup.Provider (ValueOutput doesn't expose one): the running
     * server's, else the client level's, else the static built-in ones.
     */
    public static HolderLookup.Provider registries()
    {
        final net.minecraft.server.MinecraftServer server = com.ldtteam.fabric.network.Networking.server();
        if (server != null)
        {
            return server.registryAccess();
        }
        if (Env.isClient())
        {
            final HolderLookup.Provider client = ClientEnv.registryAccess();
            if (client != null)
            {
                return client;
            }
        }
        return net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
    }

    /** Was {@code stack.saveOptional(provider)}: an empty stack is an empty tag. */
    public static net.minecraft.nbt.Tag saveOptional(final net.minecraft.world.item.ItemStack stack, final HolderLookup.Provider registries)
    {
        if (stack.isEmpty())
        {
            return new CompoundTag();
        }
        return net.minecraft.world.item.ItemStack.CODEC.encodeStart(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), stack)
            .getOrThrow();
    }

    /** Was {@code stack.save(provider)}. */
    public static net.minecraft.nbt.Tag save(final net.minecraft.world.item.ItemStack stack, final HolderLookup.Provider registries)
    {
        return saveOptional(stack, registries);
    }

    /** Was {@code stack.save(provider, target)}: merges the stack into target. */
    public static net.minecraft.nbt.Tag save(final net.minecraft.world.item.ItemStack stack, final HolderLookup.Provider registries, final net.minecraft.nbt.Tag target)
    {
        final net.minecraft.nbt.Tag saved = saveOptional(stack, registries);
        if (target instanceof final CompoundTag compound && saved instanceof final CompoundTag savedCompound)
        {
            compound.merge(savedCompound);
            return compound;
        }
        return saved;
    }

    /** Was {@code ItemStack.parseOptional(provider, tag)}: anything unreadable or empty is ItemStack.EMPTY. */
    public static net.minecraft.world.item.ItemStack parseOptional(final HolderLookup.Provider registries, final CompoundTag tag)
    {
        if (tag == null || tag.isEmpty())
        {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }
        return net.minecraft.world.item.ItemStack.CODEC.parse(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), tag)
            .result()
            .orElse(net.minecraft.world.item.ItemStack.EMPTY);
    }

    /** Was {@code ItemStack.parse(provider, tag)}. */
    public static Optional<net.minecraft.world.item.ItemStack> parse(final HolderLookup.Provider registries, final net.minecraft.nbt.Tag tag)
    {
        return net.minecraft.world.item.ItemStack.CODEC.parse(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), tag).result();
    }

    /** Was {@code blockEntity.saveWithId(provider)}. */
    public static CompoundTag saveWithId(final net.minecraft.world.level.block.entity.BlockEntity blockEntity, final HolderLookup.Provider registries)
    {
        final TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        blockEntity.saveWithId(output);
        return output.buildResult();
    }

    /** Was {@code blockEntity.loadWithComponents(tag, provider)}. */
    public static void loadWithComponents(final net.minecraft.world.level.block.entity.BlockEntity blockEntity, final CompoundTag tag, final HolderLookup.Provider registries)
    {
        blockEntity.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
    }

    /** Was {@code type.create(level)} followed by {@code entity.load(tag)}. */
    @Nullable
    public static Entity createAndLoad(final EntityType<?> type, final Level level, final CompoundTag tag)
    {
        final Entity entity = type.create(level, EntitySpawnReason.LOAD);
        if (entity != null)
        {
            loadEntity(entity, tag);
        }
        return entity;
    }
}
