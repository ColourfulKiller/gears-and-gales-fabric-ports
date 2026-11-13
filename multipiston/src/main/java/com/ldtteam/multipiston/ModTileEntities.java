package com.ldtteam.multipiston;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.ldtteam.fabric.registry.DeferredHolder;
import com.ldtteam.fabric.registry.DeferredRegister;

import static com.ldtteam.multipiston.MultiPiston.MOD_ID;

public final class ModTileEntities
{
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);

    private ModTileEntities() { /* prevent construction */ }

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityMultiPiston>>
      multipiston = TILE_ENTITIES.register("multipistonte", () -> net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(TileEntityMultiPiston::new, ModBlocks.multipiston.get()).build());
}
