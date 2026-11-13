package com.ldtteam.fabric;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

/**
 * NeoForge's {@code net.neoforged.neoforge.common.Tags} constants that the LDT mods use. Since 1.21 NeoForge and Fabric
 * share the {@code c:} convention tag ids, so these resolve to the same tags Fabric's ConventionalTags (and other
 * mods' data) fill.
 */
public final class Tags
{
    private Tags()
    {
    }

    private static <T> TagKey<T> c(final net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<T>> registry, final String path)
    {
        return TagKey.create(registry, Identifier.fromNamespaceAndPath("c", path));
    }

    public static final class Items
    {
        private Items()
        {
        }

        private static TagKey<Item> tag(final String path)
        {
            return c(Registries.ITEM, path);
        }

        public static final TagKey<Item> BUCKETS_WATER = tag("buckets/water");
        public static final TagKey<Item> CHESTS = tag("chests");
        public static final TagKey<Item> COBBLESTONES = tag("cobblestones");
        public static final TagKey<Item> CONCRETE_POWDERS = tag("concrete_powders");
        public static final TagKey<Item> CROPS = tag("crops");
        public static final TagKey<Item> CROPS_WHEAT = tag("crops/wheat");
        public static final TagKey<Item> DUSTS = tag("dusts");
        public static final TagKey<Item> DUSTS_REDSTONE = tag("dusts/redstone");
        public static final TagKey<Item> DYES = tag("dyes");
        public static final TagKey<Item> DYES_BLACK = tag("dyes/black");
        public static final TagKey<Item> DYES_BLUE = tag("dyes/blue");
        public static final TagKey<Item> DYES_BROWN = tag("dyes/brown");
        public static final TagKey<Item> DYES_CYAN = tag("dyes/cyan");
        public static final TagKey<Item> DYES_GRAY = tag("dyes/gray");
        public static final TagKey<Item> DYES_GREEN = tag("dyes/green");
        public static final TagKey<Item> DYES_LIGHT_BLUE = tag("dyes/light_blue");
        public static final TagKey<Item> DYES_LIGHT_GRAY = tag("dyes/light_gray");
        public static final TagKey<Item> DYES_LIME = tag("dyes/lime");
        public static final TagKey<Item> DYES_MAGENTA = tag("dyes/magenta");
        public static final TagKey<Item> DYES_ORANGE = tag("dyes/orange");
        public static final TagKey<Item> DYES_PINK = tag("dyes/pink");
        public static final TagKey<Item> DYES_PURPLE = tag("dyes/purple");
        public static final TagKey<Item> DYES_RED = tag("dyes/red");
        public static final TagKey<Item> DYES_WHITE = tag("dyes/white");
        public static final TagKey<Item> DYES_YELLOW = tag("dyes/yellow");
        public static final TagKey<Item> EGGS = tag("eggs");
        public static final TagKey<Item> END_STONES = tag("end_stones");
        public static final TagKey<Item> FOODS = tag("foods");
        public static final TagKey<Item> FOODS_BREAD = tag("foods/bread");
        public static final TagKey<Item> FOODS_CANDY = tag("foods/candy");
        public static final TagKey<Item> FOODS_COOKED_MEAT = tag("foods/cooked_meat");
        public static final TagKey<Item> FOODS_GOLDEN = tag("foods/golden");
        public static final TagKey<Item> FOODS_RAW_FISH = tag("foods/raw_fish");
        public static final TagKey<Item> FOODS_RAW_MEAT = tag("foods/raw_meat");
        public static final TagKey<Item> FOODS_SOUP = tag("foods/soup");
        public static final TagKey<Item> FOODS_VEGETABLE = tag("foods/vegetable");
        public static final TagKey<Item> GEMS = tag("gems");
        public static final TagKey<Item> GLASS_BLOCKS = tag("glass_blocks");
        public static final TagKey<Item> GLASS_PANES = tag("glass_panes");
        public static final TagKey<Item> GLAZED_TERRACOTTAS = tag("glazed_terracottas");
        public static final TagKey<Item> GRAVELS = tag("gravels");
        public static final TagKey<Item> INGOTS = tag("ingots");
        public static final TagKey<Item> INGOTS_GOLD = tag("ingots/gold");
        public static final TagKey<Item> MUSHROOMS = tag("mushrooms");
        public static final TagKey<Item> NUGGETS = tag("nuggets");
        public static final TagKey<Item> ORES = tag("ores");
        public static final TagKey<Item> ORES_REDSTONE = tag("ores/redstone");
        public static final TagKey<Item> SANDS = tag("sands");
        public static final TagKey<Item> SANDSTONE_BLOCKS = tag("sandstone/blocks");
        public static final TagKey<Item> SEEDS = tag("seeds");
        public static final TagKey<Item> SHULKER_BOXES = tag("shulker_boxes");
        public static final TagKey<Item> STONES = tag("stones");
        public static final TagKey<Item> STORAGE_BLOCKS = tag("storage_blocks");
        public static final TagKey<Item> STORAGE_BLOCKS_REDSTONE = tag("storage_blocks/redstone");
        public static final TagKey<Item> STRINGS = tag("strings");
    }

    public static final class Blocks
    {
        private Blocks()
        {
        }

        private static TagKey<Block> tag(final String path)
        {
            return c(Registries.BLOCK, path);
        }

        public static final TagKey<Block> BOOKSHELVES = tag("bookshelves");
        public static final TagKey<Block> CONCRETES = tag("concretes");
        public static final TagKey<Block> STONES = tag("stones");
        public static final TagKey<Block> STORAGE_BLOCKS = tag("storage_blocks");
    }

    public static final class Biomes
    {
        private Biomes()
        {
        }

        private static TagKey<Biome> tag(final String path)
        {
            return c(Registries.BIOME, path);
        }

        public static final TagKey<Biome> IS_COLD = tag("is_cold");
        public static final TagKey<Biome> IS_DESERT = tag("is_desert");
        public static final TagKey<Biome> IS_DRY = tag("is_dry");
        public static final TagKey<Biome> IS_PLAINS = tag("is_plains");
        public static final TagKey<Biome> IS_SNOWY = tag("is_snowy");
        public static final TagKey<Biome> IS_SWAMP = tag("is_swamp");
        public static final TagKey<Biome> IS_WET = tag("is_wet");
        public static final TagKey<Biome> IS_WET_OVERWORLD = tag("is_wet/overworld");
    }
}
