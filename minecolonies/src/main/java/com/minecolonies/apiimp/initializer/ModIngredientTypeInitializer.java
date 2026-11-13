package com.minecolonies.apiimp.initializer;

import com.ldtteam.fabric.crafting.IngredientType;
import com.minecolonies.api.crafting.CountedIngredient;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.recipes.FoodIngredient;
import com.minecolonies.core.recipes.PlantIngredient;
import net.minecraft.resources.Identifier;

/**
 * Custom recipe ingredient types. Fabric port: NeoForge's INGREDIENT_TYPES registry entries are Fabric custom
 * ingredient serializers, registered by {@link #register()} from MineColonies#onInitialize.
 */
public class ModIngredientTypeInitializer
{
    private ModIngredientTypeInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModIngredientTypeInitializer but this is a Utility class.");
    }

    public static final IngredientType<CountedIngredient> COUNTED_INGREDIENT_TYPE = new IngredientType<>(CountedIngredient.CODEC);
    public static final IngredientType<PlantIngredient> PLANT_INGREDIENT_TYPE = new IngredientType<>(PlantIngredient.CODEC);
    public static final IngredientType<FoodIngredient> FOOD_INGREDIENT_TYPE = new IngredientType<>(FoodIngredient.CODEC);

    public static void register()
    {
        COUNTED_INGREDIENT_TYPE.register(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "counted"));
        PLANT_INGREDIENT_TYPE.register(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "plant"));
        FOOD_INGREDIENT_TYPE.register(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "food"));
    }
}