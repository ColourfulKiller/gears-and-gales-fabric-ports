package com.minecolonies.core.client.render;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.core.entity.other.NewBobberEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Renders the citizen fisherman's bobber and line (Fabric 1.21.11: follows vanilla's fishing hook renderer, with the
 * line anchored at the citizen's hand).
 */
@Environment(EnvType.CLIENT)
public class RenderFishHook extends EntityRenderer<NewBobberEntity, FishingHookRenderState>
{
    private static final Identifier TEXTURE     = Identifier.withDefaultNamespace("textures/entity/fishing_hook.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityCutout(TEXTURE);

    public RenderFishHook(final EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public FishingHookRenderState createRenderState()
    {
        return new FishingHookRenderState();
    }

    @Override
    public void extractRenderState(final NewBobberEntity entity, final FishingHookRenderState state, final float partialTick)
    {
        super.extractRenderState(entity, state, partialTick);
        if (!(entity.getOwner() instanceof final AbstractEntityCitizen citizen))
        {
            state.lineOriginOffset = null;
            return;
        }

        int side = citizen.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        if (!citizen.getMainHandItem().is(Items.FISHING_ROD))
        {
            side = -side;
        }
        final float bodyRot = Mth.lerp(partialTick, citizen.yBodyRotO, citizen.yBodyRot) * ((float) Math.PI / 180F);
        final double sin = Mth.sin(bodyRot);
        final double cos = Mth.cos(bodyRot);
        final double sideOffset = side * 0.35D;
        final double handX = Mth.lerp(partialTick, citizen.xo, citizen.getX()) - cos * sideOffset - sin * 0.8D;
        final double handY = citizen.yo + citizen.getEyeHeight() + (citizen.getY() - citizen.yo) * partialTick - 0.45D + (citizen.isCrouching() ? -0.1875F : 0.0F);
        final double handZ = Mth.lerp(partialTick, citizen.zo, citizen.getZ()) - sin * sideOffset + cos * 0.8D;
        final Vec3 hook = entity.getPosition(partialTick).add(0.0, 0.25, 0.0);
        state.lineOriginOffset = new Vec3(handX, handY, handZ).subtract(hook);
    }

    @Override
    public void submit(final FishingHookRenderState state, final PoseStack poseStack, final SubmitNodeCollector collector, final CameraRenderState camera)
    {
        if (state.lineOriginOffset == null)
        {
            return;
        }
        poseStack.pushPose();
        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(camera.orientation);
        collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, consumer) -> {
            vertex(consumer, pose, state.lightCoords, 0.0F, 0, 0, 1);
            vertex(consumer, pose, state.lightCoords, 1.0F, 0, 1, 1);
            vertex(consumer, pose, state.lightCoords, 1.0F, 1, 1, 0);
            vertex(consumer, pose, state.lightCoords, 0.0F, 1, 0, 0);
        });
        poseStack.popPose();

        final float x = (float) state.lineOriginOffset.x;
        final float y = (float) state.lineOriginOffset.y;
        final float z = (float) state.lineOriginOffset.z;
        final float width = Minecraft.getInstance().getWindow().getAppropriateLineWidth();
        collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, consumer) -> {
            for (int k = 0; k < 16; k++)
            {
                final float a = fraction(k, 16);
                final float b = fraction(k + 1, 16);
                stringVertex(x, y, z, consumer, pose, a, b, width);
                stringVertex(x, y, z, consumer, pose, b, a, width);
            }
        });
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    @Override
    protected boolean affectedByCulling(final NewBobberEntity entity)
    {
        return false;
    }

    private static float fraction(final int first, final int second)
    {
        return (float) first / (float) second;
    }

    private static void vertex(final VertexConsumer consumer, final PoseStack.Pose pose, final int light, final float x, final int y, final int u, final int v)
    {
        consumer.addVertex(pose, x - 0.5F, y - 0.5F, 0.0F)
          .setColor(-1)
          .setUv(u, v)
          .setOverlay(OverlayTexture.NO_OVERLAY)
          .setLight(light)
          .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    private static void stringVertex(final float x, final float y, final float z, final VertexConsumer consumer, final PoseStack.Pose pose, final float a, final float b, final float width)
    {
        final float px = x * a;
        final float py = y * (a * a + a) * 0.5F + 0.25F;
        final float pz = z * a;
        float nx = x * b - px;
        float ny = y * (b * b + b) * 0.5F + 0.25F - py;
        float nz = z * b - pz;
        final float len = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        nx /= len;
        ny /= len;
        nz /= len;
        consumer.addVertex(pose, px, py, pz).setColor(-16777216).setNormal(pose, nx, ny, nz).setLineWidth(width);
    }
}
