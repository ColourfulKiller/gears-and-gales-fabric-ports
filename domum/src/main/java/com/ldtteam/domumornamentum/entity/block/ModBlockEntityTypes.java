package com.ldtteam.domumornamentum.entity.block;

import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.decorative.DynamicTimberFrameBlock;
import com.ldtteam.domumornamentum.util.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.ldtteam.fabric.registry.DeferredHolder;
import com.ldtteam.fabric.registry.DeferredRegister;

/**
 * Class to create the modBlocks.
 * References to the blocks can be made here
 */
public final class ModBlockEntityTypes
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

    @SuppressWarnings({"SuspiciousToArrayCall", "ConstantConditions"}) //Not really true.
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntity>> MATERIALLY_TEXTURED = BLOCK_ENTITIES.register(Constants.BlockEntityTypes.MATERIALLY_RETEXTURABLE.getPath(),
      () -> (BlockEntityType<BlockEntity>) (BlockEntityType<?>) net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(MateriallyTexturedBlockEntity::new,
        BuiltInRegistries.BLOCK.stream().filter(IMateriallyTexturedBlock.class::isInstance).toArray(Block[]::new)
        ).build()
    );

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntity>> DYNAMIC_TIMBERFRAME = BLOCK_ENTITIES.register(Constants.BlockEntityTypes.DYNAMIC_TIMBERFRAME.getPath(),
        () -> (BlockEntityType<BlockEntity>) (BlockEntityType<?>) net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(DynamicTimberFrameBlockEntity::new,
            BuiltInRegistries.BLOCK.stream().filter(DynamicTimberFrameBlock.class::isInstance).toArray(Block[]::new)
            ).build()
    );

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModBlockEntityTypes()
    {
    }
}
