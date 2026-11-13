package com.ldtteam.domumornamentum.entity.block;

import com.ldtteam.domumornamentum.DomumOrnamentum;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.domumornamentum.component.ModDataComponents;
import com.ldtteam.domumornamentum.util.MaterialTextureDataUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

import static com.ldtteam.domumornamentum.entity.block.ModBlockEntityTypes.MATERIALLY_TEXTURED;
import static com.ldtteam.domumornamentum.util.Constants.BLOCK_ENTITY_TEXTURE_DATA;

/**
 * Fabric 1.21.11 port: saves through ValueOutput / ValueInput (1.21.6+), and hands its texture data to the block model
 * as Fabric render data ({@link #getRenderData()}) instead of NeoForge ModelData.
 */
public class MateriallyTexturedBlockEntity extends AbstractMateriallyTexturedBlockEntity
{
    private MaterialTextureData textureData = MaterialTextureData.EMPTY;

    public MateriallyTexturedBlockEntity(BlockPos pos, BlockState state)
    {
        super(MATERIALLY_TEXTURED.get(), pos, state);
    }

    @Override
    public void updateTextureDataWith(final MaterialTextureData materialTextureData)
    {
        this.textureData = materialTextureData;

        if (this.textureData.isEmpty())
        {
            this.textureData = MaterialTextureDataUtil.generateRandomTextureDataFrom(this.getBlockState().getBlock());
        }
        else if (getBlockState().getBlock() instanceof final IMateriallyTexturedBlock materiallyTexturedBlock)
        {
            textureData = textureData.retainComponentsFromBlock(materiallyTexturedBlock);
        }

        this.requestModelDataUpdate();
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(final HolderLookup.Provider provider)
    {
        return this.saveWithoutMetadata(provider);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(final ValueOutput output)
    {
        super.saveAdditional(output);
        // this is still needed even with data components as of 1.21
        output.store(BLOCK_ENTITY_TEXTURE_DATA, MaterialTextureData.CODEC, textureData);
    }

    @Override
    protected void loadAdditional(final ValueInput input)
    {
        super.loadAdditional(input);
        try
        {
            input.read(BLOCK_ENTITY_TEXTURE_DATA, MaterialTextureData.CODEC).ifPresent(this::updateTextureDataWith);
        }
        catch (final RuntimeException e)
        {
            DomumOrnamentum.LOGGER.error("Bad texture data in block entity at " + worldPosition, e);
        }
    }

    /**
     * NeoForge's requestModelDataUpdate: on the client, re-render this block so the model picks up the new texture data.
     */
    public void requestModelDataUpdate()
    {
        if (level != null && level.isClientSide())
        {
            level.setBlocksDirty(worldPosition, Blocks.AIR.defaultBlockState(), getBlockState());
        }
    }

    /**
     * Read by MateriallyTexturedBlockStateModel through the chunk's block view (Fabric render data).
     */
    @Override
    public Object getRenderData()
    {
        return this.textureData;
    }

    @Override
    @NotNull
    public MaterialTextureData getTextureData()
    {
        return textureData;
    }

    @Override
    protected void applyImplicitComponents(final DataComponentGetter components)
    {
        super.applyImplicitComponents(components);
        updateTextureDataWith(components.getOrDefault(ModDataComponents.TEXTURE_DATA.get(), MaterialTextureData.EMPTY));
    }

    @Override
    protected void collectImplicitComponents(final DataComponentMap.Builder componentBuilder)
    {
        super.collectImplicitComponents(componentBuilder);
        componentBuilder.set(ModDataComponents.TEXTURE_DATA.get(), textureData);
    }

    @Override
    public void removeComponentsFromTag(final ValueOutput output)
    {
        output.discard(BLOCK_ENTITY_TEXTURE_DATA);
    }
}
