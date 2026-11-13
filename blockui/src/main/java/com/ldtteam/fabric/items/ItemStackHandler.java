package com.ldtteam.fabric.items;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

/**
 * A plain list-backed item handler (NeoForge's ItemStackHandler), including its NBT format
 * ({@code Items: [{Slot, ...stack}], Size}).
 */
public class ItemStackHandler implements IItemHandlerModifiable
{
    protected NonNullList<ItemStack> stacks;

    public ItemStackHandler()
    {
        this(1);
    }

    public ItemStackHandler(final int size)
    {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public ItemStackHandler(final NonNullList<ItemStack> stacks)
    {
        this.stacks = stacks;
    }

    public void setSize(final int size)
    {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    public void setStackInSlot(final int slot, final ItemStack stack)
    {
        validateSlotIndex(slot);
        stacks.set(slot, stack);
        onContentsChanged(slot);
    }

    @Override
    public int getSlots()
    {
        return stacks.size();
    }

    @Override
    public ItemStack getStackInSlot(final int slot)
    {
        validateSlotIndex(slot);
        return stacks.get(slot);
    }

    @Override
    public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate)
    {
        if (stack.isEmpty() || !isItemValid(slot, stack))
        {
            return stack;
        }
        validateSlotIndex(slot);
        final ItemStack existing = stacks.get(slot);
        int limit = getStackLimit(slot, stack);
        if (!existing.isEmpty())
        {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
            {
                return stack;
            }
            limit -= existing.getCount();
        }
        if (limit <= 0)
        {
            return stack;
        }
        final boolean reachedLimit = stack.getCount() > limit;
        if (!simulate)
        {
            if (existing.isEmpty())
            {
                stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
            }
            else
            {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }
        return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
    {
        if (amount == 0)
        {
            return ItemStack.EMPTY;
        }
        validateSlotIndex(slot);
        final ItemStack existing = stacks.get(slot);
        if (existing.isEmpty())
        {
            return ItemStack.EMPTY;
        }
        final int toExtract = Math.min(amount, existing.getMaxStackSize());
        if (existing.getCount() <= toExtract)
        {
            if (!simulate)
            {
                stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
                return existing;
            }
            return existing.copy();
        }
        if (!simulate)
        {
            stacks.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
            onContentsChanged(slot);
        }
        return existing.copyWithCount(toExtract);
    }

    @Override
    public int getSlotLimit(final int slot)
    {
        return 99;
    }

    protected int getStackLimit(final int slot, final ItemStack stack)
    {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    @Override
    public boolean isItemValid(final int slot, final ItemStack stack)
    {
        return true;
    }

    public CompoundTag serializeNBT(final HolderLookup.Provider provider)
    {
        final ListTag list = new ListTag();
        for (int i = 0; i < stacks.size(); i++)
        {
            if (!stacks.get(i).isEmpty())
            {
                final CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                list.add(ItemStack.CODEC.encode(stacks.get(i), provider.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), itemTag).getOrThrow());
            }
        }
        final CompoundTag nbt = new CompoundTag();
        nbt.put("Items", list);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }

    public void deserializeNBT(final HolderLookup.Provider provider, final CompoundTag nbt)
    {
        setSize(nbt.getIntOr("Size", stacks.size()));
        final ListTag list = nbt.getListOrEmpty("Items");
        for (int i = 0; i < list.size(); i++)
        {
            final CompoundTag itemTags = list.getCompoundOrEmpty(i);
            final int slot = itemTags.getIntOr("Slot", -1);
            if (slot >= 0 && slot < stacks.size())
            {
                ItemStack.CODEC.parse(provider.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), itemTags).result().ifPresent(stack -> stacks.set(slot, stack));
            }
        }
        onLoad();
    }

    protected void validateSlotIndex(final int slot)
    {
        if (slot < 0 || slot >= stacks.size())
        {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
        }
    }

    protected void onLoad()
    {
    }

    protected void onContentsChanged(final int slot)
    {
    }
}