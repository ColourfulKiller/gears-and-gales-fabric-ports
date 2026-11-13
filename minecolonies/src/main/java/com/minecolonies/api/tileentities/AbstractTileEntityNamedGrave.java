package com.minecolonies.api.tileentities;

import net.minecraft.core.Direction;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.minecolonies.api.util.constant.Constants.TAG_STRING;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_CONTENT;

public class AbstractTileEntityNamedGrave extends BlockEntity
{
    /**
     * The position it faces.
     */
    public static final EnumProperty<Direction> FACING       = HorizontalDirectionalBlock.FACING;

    /**
     * The text displayed on the name plate
     */
    private ArrayList<String> textLines = new ArrayList<>();

    public AbstractTileEntityNamedGrave(BlockEntityType<?> tileEntityTypeIn, final BlockPos pos, final BlockState state)
    {
        super(tileEntityTypeIn, pos, state);
        textLines.add("Unknown Citizen");
    }

    public ArrayList<String> getTextLines()
    {
        return textLines;
    }

    public void setTextLines(final ArrayList<String> content)
    {
        this.textLines = content;
        setChanged();
    }

    @Override
    public void loadAdditional(final net.minecraft.world.level.storage.ValueInput valueInput)
    {
        final CompoundTag compound = com.ldtteam.fabric.nbt.NbtIO.tag(valueInput);
        final net.minecraft.core.HolderLookup.Provider provider = valueInput.lookup();
        super.loadAdditional(valueInput);

        textLines.clear();
        if (compound.contains(TAG_CONTENT))
        {
            final ListTag lines = compound.getListOrEmpty(TAG_CONTENT);
            for (int i = 0; i < lines.size(); i++)
            {
                final String line = lines.getStringOr(i, "");
                textLines.add(line);
            }
        }
    }

    @Override
    public void saveAdditional(final net.minecraft.world.level.storage.ValueOutput valueOutput)
    {
        final CompoundTag compound = com.ldtteam.fabric.nbt.NbtIO.tag(valueOutput);
        final net.minecraft.core.HolderLookup.Provider provider = com.ldtteam.fabric.nbt.NbtIO.registries();
        super.saveAdditional(valueOutput);

        @NotNull final ListTag lines = new ListTag();
        for (@NotNull final String line : textLines)
        {
            lines.add(StringTag.valueOf(line));
        }
        compound.put(TAG_CONTENT, lines);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag(@NotNull final HolderLookup.Provider provider)
    {
        return com.ldtteam.fabric.nbt.NbtIO.saveWithId(this, provider);
    }

    // NeoForge onDataPacket hook removed: vanilla 1.21.11 applies update packets / tags via loadWithComponents -> loadAdditional

    // NeoForge handleUpdateTag hook removed: vanilla 1.21.11 applies update packets / tags via loadWithComponents -> loadAdditional

    @Override
    public void setChanged()
    {
        if (level != null)
        {
            WorldUtil.markChunkDirty(level, worldPosition);
        }
    }
}
