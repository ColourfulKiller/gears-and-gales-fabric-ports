package com.minecolonies.api.client.render.modeltype;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;

/**
 * Citizen model. Fabric 1.21.11 port: animates from a {@link CitizenRenderState} instead of the entity.
 */
public class CitizenModel extends HumanoidModel<CitizenRenderState>
{
    /**
     * Working render meta.
     */
    private static final String RENDER_META_WORKING = "working";

    public static boolean isItApril1st = false;

    public CitizenModel(final ModelPart part)
    {
        super(part, RenderTypes::entityCutoutNoCull);
    }

    @Override
    public void setupAnim(@NotNull final CitizenRenderState citizen)
    {
        super.setupAnim(citizen);
        if (body.xRot == 0)
        {
            body.xRot = getActualRotation(citizen);
        }

        if (head.xRot == 0)
        {
            head.xRot = getActualRotation(citizen);
        }

        head.visible = !citizen.hasCustomTextureHead;
        hat.visible = !citizen.hasCustomTextureHead;

        if (isItApril1st)
        {
            switch (citizen.civilianId % 7)
            {
                case 0 -> leftArm.visible = false;
                case 1 -> rightArm.visible = false;
                case 2 -> body.visible = false;
                case 3 -> head.visible = false;
                case 4 -> hat.visible = false;
                case 5 -> leftLeg.visible = false;
                case 6 -> rightLeg.visible = false;
                default ->
                {
                }
            }
        }
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    /**
     * Override to change body rotation.
     *
     * @return the rotation.
     */
    public float getActualRotation(@NotNull final CitizenRenderState entity)
    {
        return 0;
    }

    /**
     * Check if the citizen is supposed to be working.
     * @param citizen the citizen to check.
     * @return true if so.
     */
    public boolean isWorking(final CitizenRenderState citizen)
    {
        return citizen.getRenderMetadata().contains(RENDER_META_WORKING);
    }

    /**
     * Check if the hat should be displayed.
     * @param citizen the citizen to check.
     * @return true if so.
     */
    public boolean displayHat(final CitizenRenderState citizen)
    {
        return citizen.getPose() != Pose.SLEEPING && !citizen.hasItemInSlot(EquipmentSlot.HEAD) && citizen.displayHat;
    }
}
