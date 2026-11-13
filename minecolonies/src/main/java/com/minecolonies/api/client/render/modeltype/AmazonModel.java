package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMonster;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import org.jetbrains.annotations.NotNull;

/**
 * Amazon model.
 */
public class AmazonModel extends HumanoidModel<RaiderRenderState>
{
    public AmazonModel(final ModelPart part)
    {
        super(part);
    }

    @Override
    public void setupAnim(final RaiderRenderState entity)
    {
        super.setupAnim(entity);
        final float limbSwing = entity.walkAnimationPos;
        final float limbSwingAmount = entity.walkAnimationSpeed;
        final float ageInTicks = entity.ageInTicks;
        final float netHeadYaw = entity.yRot;
        final float headPitch = entity.xRot;
        head.y -= 3;
        rightLeg.y -= 3.5;
        leftLeg.y -= 3.5;
        rightArm.y -= 2;
        leftArm.y -= 2;
    }
}
