package com.ldtteam.fabric.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Fabric replacement for NeoForge's DeferredRegister, shared by the LDT Team mod ports.
 * <p>
 * Entries are queued with register(...) and created + registered, in order, when {@link #register()} runs (call it from
 * the mod's main entrypoint, blocks before items). While a Block or Item is being created its id is published through
 * {@link PendingIds}, so 1.21.1 code that never calls Properties.setId keeps working.
 */
public class DeferredRegister<T>
{
    protected final ResourceKey<? extends Registry<T>> registryKey;
    protected final String namespace;
    private final Map<DeferredHolder<T, ? extends T>, Supplier<? extends T>> entries = new LinkedHashMap<>();
    private boolean registered = false;

    protected DeferredRegister(final ResourceKey<? extends Registry<T>> registryKey, final String namespace)
    {
        this.registryKey = registryKey;
        this.namespace = namespace;
    }

    public static <T> DeferredRegister<T> create(final ResourceKey<? extends Registry<T>> registryKey, final String namespace)
    {
        return new DeferredRegister<>(registryKey, namespace);
    }

    @SuppressWarnings("unchecked")
    public static <T> DeferredRegister<T> create(final Registry<T> registry, final String namespace)
    {
        return new DeferredRegister<>((ResourceKey<? extends Registry<T>>) registry.key(), namespace);
    }

    public static Blocks createBlocks(final String namespace)
    {
        return new Blocks(namespace);
    }

    public static Items createItems(final String namespace)
    {
        return new Items(namespace);
    }

    public static DataComponents createDataComponents(final String namespace)
    {
        return new DataComponents(namespace);
    }

    public <I extends T> DeferredHolder<T, I> register(final String name, final Supplier<? extends I> supplier)
    {
        return register(name, id -> supplier.get());
    }

    public <I extends T> DeferredHolder<T, I> register(final String name, final Function<Identifier, ? extends I> factory)
    {
        if (registered)
        {
            throw new IllegalStateException("Cannot add " + name + " to " + registryKey + ": already registered");
        }
        final Identifier id = Identifier.fromNamespaceAndPath(namespace, name);
        final DeferredHolder<T, I> holder = createHolder(registryKey, id);
        if (entries.putIfAbsent(holder, () -> factory.apply(id)) != null)
        {
            throw new IllegalArgumentException("Duplicate registration " + id);
        }
        return holder;
    }

    protected <I extends T> DeferredHolder<T, I> createHolder(final ResourceKey<? extends Registry<T>> registryKey, final Identifier id)
    {
        return DeferredHolder.create(registryKey, id);
    }

    /**
     * NeoForge: register(IEventBus). Creates and registers every queued entry now.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void register()
    {
        if (registered)
        {
            return;
        }
        registered = true;
        final Registry<T> registry = (Registry<T>) Objects.requireNonNull(BuiltInRegistries.REGISTRY.getValue(registryKey.identifier()),
            () -> "Unknown registry " + registryKey);
        final boolean blocks = registryKey.equals(Registries.BLOCK);
        final boolean items = registryKey.equals(Registries.ITEM);

        for (final Map.Entry<DeferredHolder<T, ? extends T>, Supplier<? extends T>> entry : entries.entrySet())
        {
            final ResourceKey<T> key = entry.getKey().getKey();
            if (blocks)
            {
                PendingIds.BLOCK.set((ResourceKey) key);
            }
            if (items)
            {
                PendingIds.ITEM.set((ResourceKey) key);
            }
            try
            {
                final T value = entry.getValue().get();
                final Holder<T> holder = Registry.registerForHolder(registry, key, value);
                entry.getKey().bind((Holder) holder);
            }
            finally
            {
                PendingIds.BLOCK.remove();
                PendingIds.ITEM.remove();
            }
        }
    }

    public Collection<DeferredHolder<T, ? extends T>> getEntries()
    {
        return Collections.unmodifiableSet(entries.keySet());
    }

    public ResourceKey<? extends Registry<T>> getRegistryKey()
    {
        return registryKey;
    }

    public String getNamespace()
    {
        return namespace;
    }

    // ====================================================================== specialised registers

    public static class Blocks extends DeferredRegister<Block>
    {
        protected Blocks(final String namespace)
        {
            super(Registries.BLOCK, namespace);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <B extends Block> DeferredBlock<B> register(final String name, final Supplier<? extends B> supplier)
        {
            return (DeferredBlock<B>) super.register(name, supplier);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <B extends Block> DeferredBlock<B> register(final String name, final Function<Identifier, ? extends B> factory)
        {
            return (DeferredBlock<B>) super.register(name, factory);
        }

        public <B extends Block> DeferredBlock<B> registerBlock(final String name,
            final Function<BlockBehaviour.Properties, ? extends B> factory,
            final BlockBehaviour.Properties properties)
        {
            return register(name, id -> factory.apply(properties.setId(ResourceKey.create(Registries.BLOCK, id))));
        }

        public DeferredBlock<Block> registerSimpleBlock(final String name, final BlockBehaviour.Properties properties)
        {
            return registerBlock(name, Block::new, properties);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected <I extends Block> DeferredHolder<Block, I> createHolder(final ResourceKey<? extends Registry<Block>> registryKey, final Identifier id)
        {
            return (DeferredHolder<Block, I>) DeferredBlock.createBlock(id);
        }
    }

    public static class Items extends DeferredRegister<Item>
    {
        protected Items(final String namespace)
        {
            super(Registries.ITEM, namespace);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <I extends Item> DeferredItem<I> register(final String name, final Supplier<? extends I> supplier)
        {
            return (DeferredItem<I>) super.register(name, supplier);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <I extends Item> DeferredItem<I> register(final String name, final Function<Identifier, ? extends I> factory)
        {
            return (DeferredItem<I>) super.register(name, factory);
        }

        public <I extends Item> DeferredItem<I> registerItem(final String name, final Function<Item.Properties, ? extends I> factory, final Item.Properties properties)
        {
            return register(name, id -> factory.apply(properties.setId(ResourceKey.create(Registries.ITEM, id))));
        }

        public <I extends Item> DeferredItem<I> registerItem(final String name, final Function<Item.Properties, ? extends I> factory)
        {
            return registerItem(name, factory, new Item.Properties());
        }

        public DeferredItem<Item> registerSimpleItem(final String name, final Item.Properties properties)
        {
            return registerItem(name, Item::new, properties);
        }

        public DeferredItem<Item> registerSimpleItem(final String name)
        {
            return registerSimpleItem(name, new Item.Properties());
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(final String name, final Supplier<? extends Block> block, final Item.Properties properties)
        {
            return registerItem(name, p -> new BlockItem(block.get(), p.useBlockDescriptionPrefix()), properties);
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(final String name, final Supplier<? extends Block> block)
        {
            return registerSimpleBlockItem(name, block, new Item.Properties());
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(final DeferredHolder<Block, ? extends Block> block, final Item.Properties properties)
        {
            return registerSimpleBlockItem(block.getId().getPath(), block, properties);
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(final DeferredHolder<Block, ? extends Block> block)
        {
            return registerSimpleBlockItem(block, new Item.Properties());
        }

        @Override
        @SuppressWarnings("unchecked")
        protected <I extends Item> DeferredHolder<Item, I> createHolder(final ResourceKey<? extends Registry<Item>> registryKey, final Identifier id)
        {
            return (DeferredHolder<Item, I>) DeferredItem.createItem(id);
        }
    }

    public static class DataComponents extends DeferredRegister<DataComponentType<?>>
    {
        protected DataComponents(final String namespace)
        {
            super(Registries.DATA_COMPONENT_TYPE, namespace);
        }

        public <D> DeferredHolder<DataComponentType<?>, DataComponentType<D>> registerComponentType(final String name,
            final UnaryOperator<DataComponentType.Builder<D>> builder)
        {
            return register(name, () -> builder.apply(DataComponentType.builder()).build());
        }
    }
}
