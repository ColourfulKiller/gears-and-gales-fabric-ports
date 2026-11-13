package com.ldtteam.fabric.nbt;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;

/** NeoForge's INBTSerializable (1.21 form, with registries). */
public interface INBTSerializable<T extends Tag>
{
    T serializeNBT(HolderLookup.Provider provider);

    void deserializeNBT(HolderLookup.Provider provider, T nbt);
}