package com.ldtteam.fabric.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

/**
 * Since 1.21.2 every Block / Item must have its registry id in its Properties before it is constructed. NeoForge 1.21.1
 * code never sets it. {@link DeferredRegister} puts the id it is about to register here, and mixins on
 * BlockBehaviour.Properties / Item.Properties (com.ldtteam.fabric.mixin) fill it in when it is missing.
 */
public final class PendingIds
{
    static final ThreadLocal<ResourceKey<Block>> BLOCK = new ThreadLocal<>();
    static final ThreadLocal<ResourceKey<Item>> ITEM = new ThreadLocal<>();

    private PendingIds() {}

    /** Namespace of placeholder ids, given to blocks / items built before their id is known (fixed on registration). */
    public static final String PLACEHOLDER_NAMESPACE = "ldtteam_fabric";
    private static final java.util.concurrent.atomic.AtomicInteger PLACEHOLDERS = new java.util.concurrent.atomic.AtomicInteger();

    public static ResourceKey<Block> placeholderBlock()
    {
        return ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, placeholderId());
    }

    public static ResourceKey<Item> placeholderItem()
    {
        return ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, placeholderId());
    }

    private static net.minecraft.resources.Identifier placeholderId()
    {
        return net.minecraft.resources.Identifier.fromNamespaceAndPath(PLACEHOLDER_NAMESPACE, "unregistered_" + PLACEHOLDERS.incrementAndGet());
    }

    public static @Nullable ResourceKey<Block> block()
    {
        return BLOCK.get();
    }

    public static @Nullable ResourceKey<Item> item()
    {
        return ITEM.get();
    }
}
