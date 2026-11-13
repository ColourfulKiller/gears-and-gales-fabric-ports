package com.ldtteam.fabric;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.Nullable;

/**
 * 1.21.2-1.21.5 removed ArmorItem / SwordItem / DiggerItem / TieredItem: armor and tools are plain items with
 * components (equippable, tool, attribute modifiers) and tags. These answer the questions the LDT code asked with
 * {@code instanceof} checks, for vanilla and modded items alike.
 */
public final class EquipmentHooks
{
    private EquipmentHooks()
    {
    }

    /** Was {@code stack.getItem() instanceof ArmorItem}: worn in an armor slot and tagged / attributed as armor. */
    public static boolean isArmor(final ItemStack stack)
    {
        return armorSlot(stack) != null;
    }

    public static boolean isArmor(final Item item)
    {
        return isArmor(item.getDefaultInstance());
    }

    /** Was {@code ((ArmorItem) item).getEquipmentSlot()}: the humanoid armor slot, or null if not armor. */
    @Nullable
    public static EquipmentSlot armorSlot(final ItemStack stack)
    {
        final Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.slot().getType() != EquipmentSlot.Type.HUMANOID_ARMOR)
        {
            return null;
        }
        final boolean tagged = stack.is(ItemTags.HEAD_ARMOR) || stack.is(ItemTags.CHEST_ARMOR) || stack.is(ItemTags.LEG_ARMOR) || stack.is(ItemTags.FOOT_ARMOR);
        final boolean attributed = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY)
            .modifiers().stream().anyMatch(m -> m.attribute().is(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR));
        return tagged || attributed ? equippable.slot() : null;
    }

    @Nullable
    public static EquipmentSlot armorSlot(final Item item)
    {
        return armorSlot(item.getDefaultInstance());
    }

    /** Was {@code instanceof SwordItem}. */
    public static boolean isSword(final ItemStack stack)
    {
        return stack.is(ItemTags.SWORDS);
    }

    /** Was {@code instanceof DiggerItem}: pickaxes, axes, shovels and hoes. */
    public static boolean isDigger(final ItemStack stack)
    {
        return stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.AXES) || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES);
    }

    /** Was {@code instanceof TieredItem}: has a tool or weapon component. */
    public static boolean isTiered(final ItemStack stack)
    {
        return stack.has(DataComponents.TOOL) || stack.has(DataComponents.WEAPON) || isSword(stack) || isDigger(stack);
    }

    private static final net.minecraft.world.item.ToolMaterial[] MATERIALS = {
        net.minecraft.world.item.ToolMaterial.WOOD, net.minecraft.world.item.ToolMaterial.GOLD, net.minecraft.world.item.ToolMaterial.STONE,
        net.minecraft.world.item.ToolMaterial.COPPER, net.minecraft.world.item.ToolMaterial.IRON, net.minecraft.world.item.ToolMaterial.DIAMOND,
        net.minecraft.world.item.ToolMaterial.NETHERITE};

    /**
     * Was {@code ((TieredItem) item).getTier()}: items no longer keep their ToolMaterial, so it is matched by the repair
     * material (vanilla and most modded tools set it from the material), falling back to the closest durability.
     *
     * @return null if the stack is not a tool / weapon
     */
    @Nullable
    public static net.minecraft.world.item.ToolMaterial toolMaterial(final ItemStack stack)
    {
        if (!isTiered(stack))
        {
            return null;
        }
        final net.minecraft.world.item.enchantment.Repairable repairable = stack.get(DataComponents.REPAIRABLE);
        if (repairable != null)
        {
            for (final net.minecraft.world.item.ToolMaterial material : MATERIALS)
            {
                final java.util.Optional<net.minecraft.core.HolderSet.Named<Item>> tag =
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.get(material.repairItems());
                if (tag.isPresent() && repairable.items().stream().allMatch(h -> tag.get().contains(h)) && repairable.items().size() > 0)
                {
                    return material;
                }
            }
        }
        net.minecraft.world.item.ToolMaterial closest = net.minecraft.world.item.ToolMaterial.WOOD;
        for (final net.minecraft.world.item.ToolMaterial material : MATERIALS)
        {
            if (Math.abs(material.durability() - stack.getMaxDamage()) < Math.abs(closest.durability() - stack.getMaxDamage()))
            {
                closest = material;
            }
        }
        return closest;
    }

    /** Was {@code ((SwordItem) item).getDamage(stack)}: main-hand attack damage the item adds. */
    public static float attackDamage(final ItemStack stack)
    {
        final double[] total = {0};
        stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY)
            .forEach(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
                if (attribute.is(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE))
                {
                    total[0] += modifier.amount();
                }
            });
        return (float) total[0];
    }

    /** NeoForge's Tiers.values() order (wood, stone, iron, diamond, gold, netherite). */
    public static final net.minecraft.world.item.ToolMaterial[] VANILLA_TIER_ORDER = {
        net.minecraft.world.item.ToolMaterial.WOOD, net.minecraft.world.item.ToolMaterial.STONE, net.minecraft.world.item.ToolMaterial.IRON,
        net.minecraft.world.item.ToolMaterial.DIAMOND, net.minecraft.world.item.ToolMaterial.GOLD, net.minecraft.world.item.ToolMaterial.NETHERITE};
}