package com.ldtteam.structurize.blockentities;

import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.ldtteam.fabric.registry.DeferredHolder;
import com.ldtteam.fabric.registry.DeferredRegister;

public final class ModBlockEntities
{
    private ModBlockEntities() { /* prevent construction */ }

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityTagSubstitution>> TAG_SUBSTITUTION = BLOCK_ENTITIES.register("tagsubstitution",
      () -> net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(BlockEntityTagSubstitution::new, ModBlocks.blockTagSubstitution.get()).build());
}
