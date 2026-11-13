package com.minecolonies.core.client.render.mobs;

import com.minecolonies.api.client.render.modeltype.RaiderRenderState;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMonster;
import com.minecolonies.core.client.render.RenderUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Shared base of the raider renderers (Fabric 1.21.11 port of the per-culture abstract renderers): humanoid model,
 * held items, player armor, MineColonies' arm poses and the raider's texture variant in the render state.
 */
public abstract class AbstractRaiderRenderer<T extends AbstractEntityMinecoloniesMonster, M extends HumanoidModel<RaiderRenderState>>
    extends HumanoidMobRenderer<T, RaiderRenderState, M>
{
    protected AbstractRaiderRenderer(final EntityRendererProvider.Context context, final M model, final float shadowSize)
    {
        super(context, model, shadowSize);
        this.addLayer(new HumanoidArmorLayer<>(this, ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new), context.getEquipmentRenderer()));
    }

    @Override
    public RaiderRenderState createRenderState()
    {
        return new RaiderRenderState();
    }

    @Override
    public void extractRenderState(final T raider, final RaiderRenderState state, final float partialTick)
    {
        super.extractRenderState(raider, state, partialTick);
        state.textureId = raider.getTextureId();
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(final T raider, final HumanoidArm arm)
    {
        // RenderUtils swaps hands for left-handed mobs itself, so the right arm always asks for the main hand.
        return RenderUtils.getArmPose(raider, arm == HumanoidArm.RIGHT ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
    }
}