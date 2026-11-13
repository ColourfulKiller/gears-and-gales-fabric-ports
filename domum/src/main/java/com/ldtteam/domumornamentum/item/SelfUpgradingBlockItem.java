package com.ldtteam.domumornamentum.item;

import com.ldtteam.domumornamentum.DomumOrnamentum;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import static com.ldtteam.domumornamentum.util.Constants.BLOCK_ENTITY_TEXTURE_DATA;
import static com.ldtteam.domumornamentum.util.Constants.TYPE_BLOCK_PROPERTY;

/**
 * BlockItem with own DFU on tag load/set
 */
public class SelfUpgradingBlockItem extends BlockItem
{
    public SelfUpgradingBlockItem(final Block block, final Properties properties)
    {
        super(block, properties);
    }

    /**
     * Fabric port: the 1.21.1 version migrated pre-1.21 item NBT on load through a NeoForge hook. A Fabric world never
     * held those items, so this is a no-op kept for callers.
     */
    static void upgrade(final ItemStack itemStack)
    {
    }
}