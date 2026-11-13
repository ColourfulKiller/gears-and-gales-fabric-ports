package com.ldtteam.fabric.items;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Replacement for NeoForge's item-handler capabilities (Capabilities.ItemHandler.BLOCK / ENTITY / ITEM).
 * <ul>
 * <li>blocks: the block entity itself if it is an {@link IItemHandler} or a {@link Container}, otherwise Fabric's
 * Transfer API ({@link ItemStorage#SIDED}), so modded storage still works;</li>
 * <li>entities: {@link IItemHandler} or {@link Container} entities;</li>
 * <li>items: read-only views of container / bundle contents (shulker boxes etc.).</li>
 * </ul>
 */
public final class ItemHandlers
{
    private ItemHandlers()
    {
    }

    /** Per-type providers (was RegisterCapabilitiesEvent#registerBlockEntity / #registerEntity for ItemHandler). */
    @FunctionalInterface
    public interface Provider<T>
    {
        @Nullable
        IItemHandler get(T object, @Nullable Direction side);
    }

    private static final java.util.Map<net.minecraft.world.level.block.entity.BlockEntityType<?>, Provider<BlockEntity>> BLOCK_PROVIDERS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<net.minecraft.world.entity.EntityType<?>, Provider<Entity>> ENTITY_PROVIDERS = new java.util.concurrent.ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> void registerBlockEntity(final net.minecraft.world.level.block.entity.BlockEntityType<T> type, final Provider<? super T> provider)
    {
        BLOCK_PROVIDERS.put(type, (Provider<BlockEntity>) provider);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> void registerEntity(final net.minecraft.world.entity.EntityType<T> type, final Provider<? super T> provider)
    {
        ENTITY_PROVIDERS.put(type, (Provider<Entity>) provider);
    }

    @Nullable
    public static IItemHandler forBlock(final Level level, final BlockPos pos, @Nullable final BlockState state, @Nullable final BlockEntity blockEntity, @Nullable final Direction side)
    {
        if (blockEntity != null && BLOCK_PROVIDERS.containsKey(blockEntity.getType()))
        {
            return BLOCK_PROVIDERS.get(blockEntity.getType()).get(blockEntity, side);
        }
        if (blockEntity instanceof final IItemHandler handler)
        {
            return handler;
        }
        if (blockEntity instanceof final Container container)
        {
            return new InvWrapper(container);
        }
        final Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, state, blockEntity, side);
        return storage == null ? null : new StorageItemHandler(storage);
    }

    @Nullable
    public static IItemHandler forEntity(final Entity entity)
    {
        return forEntity(entity, null);
    }

    @Nullable
    public static IItemHandler forEntity(final Entity entity, @Nullable final Direction side)
    {
        if (ENTITY_PROVIDERS.containsKey(entity.getType()))
        {
            return ENTITY_PROVIDERS.get(entity.getType()).get(entity, side);
        }
        if (entity instanceof final IItemHandler handler)
        {
            return handler;
        }
        if (entity instanceof final Container container)
        {
            return new InvWrapper(container);
        }
        if (entity instanceof final net.minecraft.world.entity.LivingEntity living)
        {
            // NeoForge gave every living entity an item handler over its equipment (hands + armor)
            return new EquipmentWrapper(living);
        }
        return null;
    }

    /** Read-only view of the items stored inside an item (shulker box, bundle), or null. */
    @Nullable
    public static IItemHandler forItem(final ItemStack stack)
    {
        final ItemContainerContents container = stack.get(DataComponents.CONTAINER);
        if (container != null)
        {
            final List<ItemStack> items = new ArrayList<>();
            container.nonEmptyItemsCopy().forEach(items::add);
            return new ReadOnly(items);
        }
        final BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null)
        {
            final List<ItemStack> items = new ArrayList<>();
            bundle.itemCopyStream().forEach(items::add);
            return new ReadOnly(items);
        }
        return null;
    }

    private static final class ReadOnly extends ItemStackHandler
    {
        private ReadOnly(final List<ItemStack> items)
        {
            super(NonNullList.of(ItemStack.EMPTY, items.toArray(ItemStack[]::new)));
        }

        @Override
        public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate)
        {
            return stack;
        }

        @Override
        public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
        {
            return ItemStack.EMPTY;
        }
    }

    /**
     * IItemHandler view of a Transfer API storage. Slotted storages map slot for slot; other storages expose their
     * current non-empty views as slots (inserting into one of those inserts into the storage as a whole).
     */
    public static final class StorageItemHandler implements IItemHandler
    {
        private final Storage<ItemVariant> storage;

        public StorageItemHandler(final Storage<ItemVariant> storage)
        {
            this.storage = storage;
        }

        private List<StorageView<ItemVariant>> views()
        {
            final List<StorageView<ItemVariant>> views = new ArrayList<>();
            for (final StorageView<ItemVariant> view : storage.nonEmptyViews())
            {
                views.add(view);
            }
            return views;
        }

        @Nullable
        private StorageView<ItemVariant> view(final int slot)
        {
            if (storage instanceof final SlottedStorage<ItemVariant> slotted)
            {
                return slot >= 0 && slot < slotted.getSlotCount() ? slotted.getSlot(slot) : null;
            }
            final List<StorageView<ItemVariant>> views = views();
            return slot >= 0 && slot < views.size() ? views.get(slot) : null;
        }

        @Override
        public int getSlots()
        {
            return storage instanceof final SlottedStorage<ItemVariant> slotted ? slotted.getSlotCount() : views().size() + 1;
        }

        @Override
        public ItemStack getStackInSlot(final int slot)
        {
            final StorageView<ItemVariant> view = view(slot);
            return view == null || view.isResourceBlank() ? ItemStack.EMPTY : view.getResource().toStack((int) Math.min(Integer.MAX_VALUE, view.getAmount()));
        }

        @Override
        public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate)
        {
            if (stack.isEmpty() || !storage.supportsInsertion())
            {
                return stack;
            }
            try (Transaction transaction = Transaction.openOuter())
            {
                final ItemVariant variant = ItemVariant.of(stack);
                final long inserted = storage instanceof final SlottedStorage<ItemVariant> slotted && slot < slotted.getSlotCount()
                    ? slotted.getSlot(slot).insert(variant, stack.getCount(), transaction)
                    : storage.insert(variant, stack.getCount(), transaction);
                if (!simulate)
                {
                    transaction.commit();
                }
                return inserted >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - (int) inserted);
            }
        }

        @Override
        public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
        {
            final StorageView<ItemVariant> view = view(slot);
            if (amount <= 0 || view == null || view.isResourceBlank() || !storage.supportsExtraction())
            {
                return ItemStack.EMPTY;
            }
            final ItemVariant variant = view.getResource();
            try (Transaction transaction = Transaction.openOuter())
            {
                final long extracted = view.extract(variant, amount, transaction);
                if (!simulate)
                {
                    transaction.commit();
                }
                return extracted <= 0 ? ItemStack.EMPTY : variant.toStack((int) extracted);
            }
        }

        @Override
        public int getSlotLimit(final int slot)
        {
            final StorageView<ItemVariant> view = view(slot);
            return view == null ? 64 : (int) Math.min(Integer.MAX_VALUE, view.getCapacity());
        }

        @Override
        public boolean isItemValid(final int slot, final ItemStack stack)
        {
            return storage.supportsInsertion();
        }
    }
}
