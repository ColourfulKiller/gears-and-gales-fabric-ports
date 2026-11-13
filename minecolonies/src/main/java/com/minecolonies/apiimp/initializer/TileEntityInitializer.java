package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.tileentities.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.tileentities.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.ldtteam.fabric.registry.DeferredRegister;

public class TileEntityInitializer
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

    static
    {
        MinecoloniesTileEntities.SCARECROW = BLOCK_ENTITIES.register("scarecrow", () -> net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(TileEntityScarecrow::new, ModBlocks.blockScarecrow).build());

        MinecoloniesTileEntities.PLANTATION_FIELD = BLOCK_ENTITIES.register("plantationfield", () -> net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(TileEntityPlantationField::new, ModBlocks.blockPlantationField).build());

        MinecoloniesTileEntities.BARREL = BLOCK_ENTITIES.register("barrel", () -> net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(TileEntityBarrel::new, ModBlocks.blockBarrel).build());

        MinecoloniesTileEntities.BUILDING = BLOCK_ENTITIES.register("colonybuilding", () -> net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(TileEntityColonyBuilding::new, ModBlocks.getHuts()).build());

        MinecoloniesTileEntities.DECO_CONTROLLER = BLOCK_ENTITIES.register("decorationcontroller", () -> net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(TileEntityDecorationController::new, ModBlocks.blockDecorationPlaceholder).build());

        MinecoloniesTileEntities.RACK = BLOCK_ENTITIES.register("rack", () -> net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(TileEntityRack::new, ModBlocks.blockRack).build());

        MinecoloniesTileEntities.GRAVE = BLOCK_ENTITIES.register("grave", () -> net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(TileEntityGrave::new, ModBlocks.blockGrave).build());

        MinecoloniesTileEntities.NAMED_GRAVE = BLOCK_ENTITIES.register("namedgrave", () -> net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(TileEntityNamedGrave::new, ModBlocks.blockNamedGrave).build());

        MinecoloniesTileEntities.WAREHOUSE = BLOCK_ENTITIES.register("warehouse", () -> net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(TileEntityWareHouse::new, ModBlocks.blockHutWareHouse).build());

        MinecoloniesTileEntities.COMPOSTED_DIRT = BLOCK_ENTITIES.register("composteddirt", () -> net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(TileEntityCompostedDirt::new, ModBlocks.blockCompostedDirt).build());

        MinecoloniesTileEntities.ENCHANTER = BLOCK_ENTITIES.register("enchanter", () -> net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(TileEntityEnchanter::new, ModBlocks.blockHutEnchanter).build());

        MinecoloniesTileEntities.STASH = BLOCK_ENTITIES.register("stash", () -> net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(TileEntityStash::new, ModBlocks.blockStash).build());

        MinecoloniesTileEntities.COLONY_FLAG = BLOCK_ENTITIES.register("colony_flag", () -> net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(TileEntityColonyFlag::new, ModBlocks.blockColonyBanner, ModBlocks.blockColonyWallBanner).build());

        MinecoloniesTileEntities.COLONY_SIGN = BLOCK_ENTITIES.register("colonysign", () -> net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(TileEntityColonySign::new, ModBlocks.blockColonySign).build());
    }
}
