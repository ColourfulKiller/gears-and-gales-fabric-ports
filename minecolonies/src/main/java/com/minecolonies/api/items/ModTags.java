package com.minecolonies.api.items;

import com.minecolonies.api.util.constant.TagConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class ModTags
{
    /**
     * Flag to check if tags are already loaded.
     */
    public static boolean tagsLoaded = false;

    public static final TagKey<Block> decorationItems = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.DECORATION_ITEMS);
    public static final TagKey<Block> pathingBlocks   = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.PATHING_BLOCKS);
    public static final TagKey<Block> dangerousBlocks = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.DANGEROUS_BLOCKS);
    public static final TagKey<Block> freeClimbBlocks = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.FREE_CLIMB_BLOCKS);
    public static final TagKey<Block> tier1blocks     = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.TIER1_BLOCKS);
    public static final TagKey<Block> tier2blocks     = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.TIER2_BLOCKS);
    public static final TagKey<Block> tier3blocks     = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.TIER3_BLOCKS);
    public static final TagKey<Block> tier4blocks     = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.TIER4_BLOCKS);
    public static final TagKey<Block> tier5blocks     = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.TIER5_BLOCKS);
    public static final TagKey<Block> tier6blocks     = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.TIER6_BLOCKS);
    public static final TagKey<Block> mangroveTree    = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.MANGROVE_TREE_BLOCKS);
    public static final TagKey<Block> extraTree       = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.EXTRA_TREE_BLOCKS);
    public static final TagKey<Block> tree            = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.TREE_BLOCKS);

    public static final TagKey<Block> concreteBlocks       = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.CONCRETE_BLOCK);
    public static final TagKey<Item>  concreteItems        = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, TagConstants.CONCRETE_BLOCK);
    public static final TagKey<Block> concretePowderBlocks = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.CONCRETE_POWDER);
    public static final TagKey<Item>  concretePowderItems  = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, TagConstants.CONCRETE_POWDER);

    public static final TagKey<Block> colonyProtectionException = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.COLONYPROTECTIONEXCEPTION);
    public static final TagKey<Block> indestructible            = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.INDESTRUCTIBLE);

    public static final TagKey<Block> oreChanceBlocks = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.ORECHANCEBLOCKS);

    public static final TagKey<Block> validSpawn = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.VALIDSPAWNBLOCKS);

    public static final TagKey<Block> mushroomBlocks   = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.MUSHROOMS);
    public static final TagKey<Block> hugeMushroomBlocks   = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.MUSHROOMS_HUGE);
    public static final TagKey<Block> fungiBlocks      = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, TagConstants.FUNGI);
    public static final TagKey<Item> fungi             = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, TagConstants.FUNGI);
    public static final TagKey<Item> compostables      = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, TagConstants.COMPOSTABLES);
    public static final TagKey<Item> compostables_poor = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, TagConstants.COMPOSTABLES_POOR);
    public static final TagKey<Item> compostables_rich = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, TagConstants.COMPOSTABLES_RICH);

    public static final TagKey<Item> meshes = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, TagConstants.MESHES);

    public static final TagKey<Item> floristFlowers = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, TagConstants.FLORIST_FLOWERS);
    public static final TagKey<Item> excludedFood = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, TagConstants.EXCLUDED_FOOD);

    public static final TagKey<Item> breakable_ore = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, TagConstants.BREAKABLE_ORE);
    public static final TagKey<Item> raw_ore = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, TagConstants.RAW_ORE);

    public static final TagKey<Item> poisonous_food = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, TagConstants.POISONOUS_FOOD);

    public static final TagKey<Item> rawMeat       = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, TagConstants.RAW_MEAT);
    public static final TagKey<Item> feed           = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, TagConstants.FEED);
    public static final TagKey<Item> leather        = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, TagConstants.LEATHER);

    public static final TagKey<EntityType<?>> hostile = TagKey.create(Registries.ENTITY_TYPE, TagConstants.HOSTILE);
    public static final TagKey<EntityType<?>> mobAttackBlacklist = TagKey.create(Registries.ENTITY_TYPE, TagConstants.MOB_ATTACK_BLACKLIST);

    public static final TagKey<EntityType<?>> raiders = TagKey.create(Registries.ENTITY_TYPE, TagConstants.RAIDERS);

    public static final TagKey<EntityType<?>> freeToInteractWith = TagKey.create(Registries.ENTITY_TYPE, TagConstants.ALLOW_INTERACT);

    public static final TagKey<Item> ignoreNBT = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, TagConstants.IGNORE_NBT);

    public static final TagKey<Biome> coldBiomes = TagKey.create(Registries.BIOME, TagConstants.COLD_BIOMES);
    public static final TagKey<Biome> temperateBiomes = TagKey.create(Registries.BIOME, TagConstants.TEMPERATE_BIOMES);
    public static final TagKey<Biome> humidBiomes = TagKey.create(Registries.BIOME, TagConstants.HUMID_BIOMES);
    public static final TagKey<Biome> dryBiomes = TagKey.create(Registries.BIOME, TagConstants.DRY_BIOMES);

    public static List<TagKey<Biome>> cropBiomeTags = List.of(coldBiomes, temperateBiomes, humidBiomes, dryBiomes);

    public static final Map<String, TagKey<Item>> crafterProduct              = new HashMap<>();
    public static final Map<String, TagKey<Item>> crafterProductExclusions    = new HashMap<>();
    public static final Map<String, TagKey<Item>> crafterIngredient           = new HashMap<>();
    public static final Map<String, TagKey<Item>> crafterIngredientExclusions = new HashMap<>();
    public static final Map<String, TagKey<Item>> crafterDoIngredient         = new HashMap<>();


    /**
     * Tag specifier for Products to Include
     */
    private static final String PRODUCT = "_product";

    /**
     * Tag specifier for Products to Exclude
     */
    private static final String PRODUCT_EXCLUDED = "_product_excluded";

    /**
     * Tag specifier for Ingredients to include
     */
    private static final String INGREDIENT = "_ingredient";

    /**
     * Tag specifier for Ingredients to exclude
     */
    private static final String INGREDIENT_EXCLUDED = "_ingredient_excluded";

    /**
     * Tag specifier for Ingredients to include
     */
    private static final String DO_INGREDIENT = "_do_ingredient";

    public static void init()
    {
        initCrafterRules(TagConstants.CRAFTING_BAKER);  // both crafting and smelting
        initCrafterRules(TagConstants.CRAFTING_BLACKSMITH);
        initCrafterRules(TagConstants.CRAFTING_COOK);   // both crafting and smelting
        initCrafterRules(TagConstants.CRAFTING_DYER);
        initCrafterRules(TagConstants.CRAFTING_DYER_SMELTING);
        initCrafterRules(TagConstants.CRAFTING_FARMER);
        initCrafterRules(TagConstants.CRAFTING_FLETCHER);
        initCrafterRules(TagConstants.CRAFTING_GLASSBLOWER);
        initCrafterRules(TagConstants.CRAFTING_GLASSBLOWER_SMELTING);
        initCrafterRules(TagConstants.CRAFTING_MECHANIC);
        initCrafterRules(TagConstants.CRAFTING_PLANTATION);
        initCrafterRules(TagConstants.CRAFTING_SAWMILL);
        initCrafterRules(TagConstants.CRAFTING_STONEMASON);
        initCrafterRules(TagConstants.CRAFTING_STONE_SMELTERY);

        initCrafterRules(TagConstants.CRAFTING_REDUCEABLE);
    }

    /**
     * Initialize the four tags for a particular crafter
     * @param crafterName the string name of the crafter to initialize
     */
    private static void initCrafterRules(@NotNull final String crafterName)
    {
        final Identifier products = Identifier.fromNamespaceAndPath(MOD_ID, crafterName.concat(PRODUCT));
        final Identifier ingredients = Identifier.fromNamespaceAndPath(MOD_ID, crafterName.concat(INGREDIENT));
        final Identifier productsExcluded = Identifier.fromNamespaceAndPath(MOD_ID, crafterName.concat(PRODUCT_EXCLUDED));
        final Identifier ingredientsExcluded = Identifier.fromNamespaceAndPath(MOD_ID, crafterName.concat(INGREDIENT_EXCLUDED));
        final Identifier doIngredients = Identifier.fromNamespaceAndPath(MOD_ID, crafterName.concat(DO_INGREDIENT));

        crafterProduct.put(crafterName, net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, products));
        crafterProductExclusions.put(crafterName, net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, productsExcluded));
        crafterIngredient.put(crafterName, net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, ingredients));
        crafterIngredientExclusions.put(crafterName, net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, ingredientsExcluded));
        crafterDoIngredient.put(crafterName, net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, doIngredients));
    }

    private ModTags()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModTags. This is a utility class");
    }
}
