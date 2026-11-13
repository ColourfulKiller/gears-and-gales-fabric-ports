package com.ldtteam.fabric.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * Evaluates NeoForge-style ({@code "neoforge:conditions"}) and Fabric-style ({@code "fabric:load_conditions"})
 * conditions on custom JSON data, for reload listeners that read JSON themselves.
 * <p>
 * Supported: mod_loaded, not, and, or, true, false, item_exists (neoforge namespace) and all_mods_loaded,
 * any_mods_loaded, not, and, or, true (fabric namespace). Unknown condition types pass, so data written for other
 * loaders' extras still loads.
 */
public final class JsonConditions
{
    public static final String NEOFORGE_KEY = "neoforge:conditions";
    public static final String FABRIC_KEY   = "fabric:load_conditions";

    private JsonConditions()
    {
    }

    /** Whether every condition attached to the object holds (true when it has none). */
    public static boolean test(final JsonObject object)
    {
        return testList(object.get(NEOFORGE_KEY)) && testList(object.get(FABRIC_KEY));
    }

    private static boolean testList(final JsonElement element)
    {
        if (element == null)
        {
            return true;
        }
        if (element.isJsonObject())
        {
            return testOne(element.getAsJsonObject());
        }
        if (element.isJsonArray())
        {
            for (final JsonElement e : element.getAsJsonArray())
            {
                if (e.isJsonObject() && !testOne(e.getAsJsonObject()))
                {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean testOne(final JsonObject condition)
    {
        final String type = condition.has("type") ? condition.get("type").getAsString() : condition.has("condition") ? condition.get("condition").getAsString() : "";
        final String name = type.contains(":") ? type.substring(type.indexOf(':') + 1) : type;
        switch (name)
        {
            case "true":
                return true;
            case "false":
                return false;
            case "mod_loaded":
                return FabricLoader.getInstance().isModLoaded(alias(condition.get("modid").getAsString()));
            case "all_mods_loaded":
                for (final JsonElement id : array(condition, "values"))
                {
                    if (!FabricLoader.getInstance().isModLoaded(alias(id.getAsString())))
                    {
                        return false;
                    }
                }
                return true;
            case "any_mods_loaded":
                for (final JsonElement id : array(condition, "values"))
                {
                    if (FabricLoader.getInstance().isModLoaded(alias(id.getAsString())))
                    {
                        return true;
                    }
                }
                return false;
            case "not":
                return condition.has("value") && condition.get("value").isJsonObject() ? !testOne(condition.getAsJsonObject("value")) : true;
            case "and":
                for (final JsonElement e : array(condition, condition.has("values") ? "values" : "conditions"))
                {
                    if (e.isJsonObject() && !testOne(e.getAsJsonObject()))
                    {
                        return false;
                    }
                }
                return true;
            case "or":
                for (final JsonElement e : array(condition, condition.has("values") ? "values" : "conditions"))
                {
                    if (e.isJsonObject() && testOne(e.getAsJsonObject()))
                    {
                        return true;
                    }
                }
                return false;
            case "item_exists":
                final Identifier item = Identifier.tryParse(condition.get("item").getAsString());
                return item != null && BuiltInRegistries.ITEM.containsKey(item);
            default:
                return true;
        }
    }

    /** NeoForge mod ids whose Fabric equivalent has a different id. */
    private static String alias(final String modId)
    {
        return "neoforge".equals(modId) ? "fabric-api" : modId;
    }

    private static JsonArray array(final JsonObject object, final String key)
    {
        final JsonElement e = object.get(key);
        return e != null && e.isJsonArray() ? e.getAsJsonArray() : new JsonArray();
    }
}
