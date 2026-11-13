package com.minecolonies.api.client.render.modeltype;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Render state of a citizen / visitor (1.21.2+ entity rendering works on extracted states, not on the entity).
 * Carries what the citizen models read in setupAnim; the getters mirror the entity methods the models used.
 */
public class CitizenRenderState extends HumanoidRenderState
{
    /** The model to draw this citizen with (picked per model type, gender and custom texture). */
    public @Nullable CitizenModel model;

    public Identifier texture;
    public String renderMetadata = "";
    public int civilianId;
    public boolean hasCustomTextureHead;
    public boolean displayHat = true;
    public boolean ghost;
    public @Nullable Identifier statusIcon;
    public ItemStack mainHandItem = ItemStack.EMPTY;
    public final Map<EquipmentSlot, ItemStack> slots = new EnumMap<>(EquipmentSlot.class);

    public Pose getPose()
    {
        return pose;
    }

    public String getRenderMetadata()
    {
        return renderMetadata;
    }

    public ItemStack getMainHandItem()
    {
        return mainHandItem;
    }

    public boolean hasItemInSlot(final EquipmentSlot slot)
    {
        final ItemStack stack = slots.get(slot);
        return stack != null && !stack.isEmpty();
    }
}
