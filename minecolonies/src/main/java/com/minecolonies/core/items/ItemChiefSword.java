package com.minecolonies.core.items;

import com.minecolonies.api.entity.mobs.RaiderMobUtils;
import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarianRaider;
import com.minecolonies.api.items.IChiefSwordItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.GLOW_EFFECT_DISTANCE;
import static com.minecolonies.api.util.constant.Constants.GLOW_EFFECT_DURATION;

/**
 * Class handling the Chief Sword item.
 */
public class ItemChiefSword extends net.minecraft.world.item.Item implements IChiefSwordItem
{
    private static final int LEVITATION_EFFECT_DURATION   = 20 * 10;
    private static final int LEVITATION_EFFECT_MULTIPLIER = 2;

    /**
     * Constructor method for the Chief Sword Item
     *
     * @param properties the properties.
     */
    public ItemChiefSword(final Properties properties)
    {
        // 1.21.2: swords are components; diamond durability, damage as before (wood bonus 0 + 3)
        super(properties.sword(net.minecraft.world.item.ToolMaterial.DIAMOND, 3 - net.minecraft.world.item.ToolMaterial.DIAMOND.attackDamageBonus(), -2.4F));
    }

    @Override
    public void inventoryTick(final ItemStack stack, final net.minecraft.server.level.ServerLevel worldIn, final Entity entityIn, @org.jetbrains.annotations.Nullable final net.minecraft.world.entity.EquipmentSlot equipSlot)
    {
        final int itemSlot = -1;
        final boolean isSelected = equipSlot == net.minecraft.world.entity.EquipmentSlot.MAINHAND;
        if (entityIn instanceof Player && isSelected)
        {
            RaiderMobUtils.getBarbariansCloseToEntity(entityIn, GLOW_EFFECT_DISTANCE)
                .forEach(entity -> entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_EFFECT_DURATION, 0)));
        }
    }

    @Override
    public void hurtEnemy(final ItemStack stack, final LivingEntity target, @NotNull final LivingEntity attacker)
    {
        if (attacker instanceof Player && target instanceof AbstractEntityBarbarianRaider)
        {
            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, LEVITATION_EFFECT_DURATION, LEVITATION_EFFECT_MULTIPLIER));
        }

        super.hurtEnemy(stack, target, attacker);
    }
}
