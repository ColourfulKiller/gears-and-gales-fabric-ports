package com.ldtteam.fabric;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShieldItem;

import java.util.Set;

/**
 * Replacement for NeoForge's ItemAbilities / ItemAbility ("tool actions"). Vanilla 1.21.2+ tags every tool kind
 * ({@code #minecraft:pickaxes}, ...) and Fabric's convention tags cover shears / shields / fishing rods, so "can this
 * item do X" is answered from tags (which modded tools join), with {@link Provider} for items that decide themselves.
 */
public final class ItemAbilities
{
    private ItemAbilities()
    {
    }

    /** An ability, compared by name like NeoForge's. */
    public record ItemAbility(String name) {}

    /** Items that answer ability queries themselves (was an override of IItemExtension#canPerformAction). */
    public interface Provider
    {
        boolean canPerformAction(ItemStack stack, ItemAbility itemAbility);
    }

    public static final ItemAbility AXE_DIG = new ItemAbility("axe_dig");
    public static final ItemAbility PICKAXE_DIG = new ItemAbility("pickaxe_dig");
    public static final ItemAbility SHOVEL_DIG = new ItemAbility("shovel_dig");
    public static final ItemAbility HOE_DIG = new ItemAbility("hoe_dig");
    public static final ItemAbility HOE_TILL = new ItemAbility("till");
    public static final ItemAbility SWORD_DIG = new ItemAbility("sword_dig");
    public static final ItemAbility SWORD_SWEEP = new ItemAbility("sword_sweep");
    public static final ItemAbility SHEARS_DIG = new ItemAbility("shears_dig");
    public static final ItemAbility SHIELD_BLOCK = new ItemAbility("shield_block");
    public static final ItemAbility FISHING_ROD_CAST = new ItemAbility("fishing_rod_cast");

    public static final Set<ItemAbility> DEFAULT_AXE_ACTIONS = Set.of(AXE_DIG);
    public static final Set<ItemAbility> DEFAULT_PICKAXE_ACTIONS = Set.of(PICKAXE_DIG);
    public static final Set<ItemAbility> DEFAULT_SHOVEL_ACTIONS = Set.of(SHOVEL_DIG);
    public static final Set<ItemAbility> DEFAULT_HOE_ACTIONS = Set.of(HOE_DIG, HOE_TILL);
    public static final Set<ItemAbility> DEFAULT_SWORD_ACTIONS = Set.of(SWORD_DIG, SWORD_SWEEP);
    public static final Set<ItemAbility> DEFAULT_SHEARS_ACTIONS = Set.of(SHEARS_DIG);
    public static final Set<ItemAbility> DEFAULT_SHIELD_ACTIONS = Set.of(SHIELD_BLOCK);
    public static final Set<ItemAbility> DEFAULT_FISHING_ROD_ACTIONS = Set.of(FISHING_ROD_CAST);

    private static final TagKey<Item> SHEARS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "tools/shear"));
    private static final TagKey<Item> SHIELDS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "tools/shield"));
    private static final TagKey<Item> FISHING_RODS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "tools/fishing_rod"));

    /** Was {@code stack.canPerformAction(ability)}. */
    public static boolean canPerformAction(final ItemStack stack, final ItemAbility ability)
    {
        if (stack.isEmpty())
        {
            return false;
        }
        if (stack.getItem() instanceof final Provider provider)
        {
            return provider.canPerformAction(stack, ability);
        }
        if (DEFAULT_AXE_ACTIONS.contains(ability))
        {
            return stack.is(ItemTags.AXES);
        }
        if (DEFAULT_PICKAXE_ACTIONS.contains(ability))
        {
            return stack.is(ItemTags.PICKAXES);
        }
        if (DEFAULT_SHOVEL_ACTIONS.contains(ability))
        {
            return stack.is(ItemTags.SHOVELS);
        }
        if (DEFAULT_HOE_ACTIONS.contains(ability))
        {
            return stack.is(ItemTags.HOES);
        }
        if (DEFAULT_SWORD_ACTIONS.contains(ability))
        {
            return stack.is(ItemTags.SWORDS);
        }
        if (DEFAULT_SHEARS_ACTIONS.contains(ability))
        {
            return stack.getItem() instanceof ShearsItem || stack.is(SHEARS);
        }
        if (DEFAULT_SHIELD_ACTIONS.contains(ability))
        {
            return stack.getItem() instanceof ShieldItem || stack.is(SHIELDS);
        }
        if (DEFAULT_FISHING_ROD_ACTIONS.contains(ability))
        {
            return stack.getItem() instanceof FishingRodItem || stack.is(FISHING_RODS);
        }
        return false;
    }
}
