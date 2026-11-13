package com.minecolonies.core.client.render;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.CitizenRenderState;
import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.apiimp.initializer.ModModelTypeInitializer;
import com.minecolonies.core.client.render.worldevent.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Renderer for the citizens.
 * <p>
 * Fabric 1.21.11 port: extract the citizen into a {@link CitizenRenderState} (model choice, texture, displayed armor,
 * custom-texture head), then let vanilla's submit pipeline draw it. Display armor goes through the vanilla armor
 * layer by filling the state's equipment; a custom texture head is drawn by the vanilla custom-head layer as a
 * player skull.
 */
public class RenderBipedCitizen extends MobRenderer<AbstractEntityCitizen, CitizenRenderState, CitizenModel>
{
    private static final double  SHADOW_SIZE   = 0.5F;
    public static        boolean isItGhostTime = false;

    /**
     * Custom texture profiles by texture UUID (resolved lazily by the skull renderer).
     */
    private static final Map<UUID, ResolvableProfile> PROFILES = new HashMap<>();

    private final CitizenModel defaultModel;

    /**
     * Renders model, see {@link MobRenderer}.
     *
     * @param context the context for this Renderer.
     */
    public RenderBipedCitizen(final EntityRendererProvider.Context context)
    {
        super(context, new CitizenModel(context.bakeLayer(ModelLayers.PLAYER)), (float) SHADOW_SIZE);
        this.defaultModel = this.model;
        this.addLayer(new HumanoidArmorLayer<>(this, ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new), context.getEquipmentRenderer()));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getPlayerSkinRenderCache()));
        this.addLayer(new ItemInHandLayer<>(this));
        ModModelTypeInitializer.init(context);
    }

    @NotNull
    @Override
    public CitizenRenderState createRenderState()
    {
        return new CitizenRenderState();
    }

    @Override
    public void extractRenderState(final AbstractEntityCitizen citizen, final CitizenRenderState state, final float partialTick)
    {
        super.extractRenderState(citizen, state, partialTick);
        HumanoidMobRenderer.extractHumanoidRenderState(citizen, state, partialTick, this.itemModelResolver);
        state.rightArmPose = RenderUtils.getArmPose(citizen, InteractionHand.MAIN_HAND);
        state.leftArmPose = RenderUtils.getArmPose(citizen, InteractionHand.OFF_HAND);

        final ICitizenDataView view = citizen.getCitizenDataView();
        state.model = pickModel(citizen, view);
        state.renderMetadata = citizen.getRenderMetadata();
        state.civilianId = citizen.getCivilianID();
        state.ghost = isItGhostTime;
        state.mainHandItem = citizen.getMainHandItem().copy();
        for (final EquipmentSlot slot : EquipmentSlot.values())
        {
            state.slots.put(slot, citizen.getItemBySlot(slot));
        }

        state.texture = view != null && view.getCustomTexture() != null ? view.getCustomTexture() : citizen.getTexture();
        state.statusIcon = view != null && view.hasVisibleStatus() ? view.getStatusIcon() : null;

        final UUID customTexture = view == null ? null : view.getCustomTextureUUID();
        state.hasCustomTextureHead = customTexture != null;
        state.displayHat = view == null || (view.getDisplayArmor(EquipmentSlot.HEAD).isEmpty() && customTexture == null);

        if (view != null && view.getInventory() != null && !citizen.isInvisible())
        {
            // Display armor overrides what is worn (vanilla's armor layer reads these fields).
            state.headEquipment = armorFor(citizen, view, EquipmentSlot.HEAD, state.headEquipment);
            state.chestEquipment = armorFor(citizen, view, EquipmentSlot.CHEST, state.chestEquipment);
            state.legsEquipment = armorFor(citizen, view, EquipmentSlot.LEGS, state.legsEquipment);
            state.feetEquipment = armorFor(citizen, view, EquipmentSlot.FEET, state.feetEquipment);
        }

        if (customTexture != null)
        {
            state.wornHeadType = SkullBlock.Types.PLAYER;
            state.wornHeadProfile = PROFILES.computeIfAbsent(customTexture, id -> ResolvableProfile.createUnresolved(id));
            state.headItem.clear();
        }
    }

    private static ItemStack armorFor(final AbstractEntityCitizen citizen, final ICitizenDataView view, final EquipmentSlot slot, final ItemStack worn)
    {
        final ItemStack display = view.getDisplayArmor(slot);
        if (display.isEmpty())
        {
            return worn;
        }
        return HumanoidArmorLayer.shouldRender(display, slot) ? display.copy() : ItemStack.EMPTY;
    }

    private CitizenModel pickModel(final AbstractEntityCitizen citizen, @Nullable final ICitizenDataView view)
    {
        if (view != null && view.getCustomTexture() != null)
        {
            final CitizenModel custom = IModelTypeRegistry.getInstance().getModelType(ModModelTypes.CUSTOM_ID).getMaleModel();
            if (custom != null)
            {
                return custom;
            }
        }
        final IModelType modelType = IModelTypeRegistry.getInstance().getModelType(citizen.getModelType());
        if (modelType == null)
        {
            return defaultModel;
        }
        CitizenModel model = citizen.isFemale() ? modelType.getFemaleModel() : modelType.getMaleModel();
        if (model == null)
        {
            model = modelType.getMaleModel();
        }
        return model == null ? defaultModel : model;
    }

    @Override
    public void submit(final CitizenRenderState state, final PoseStack poseStack, final SubmitNodeCollector collector, final CameraRenderState camera)
    {
        this.model = state.model == null ? defaultModel : state.model;
        super.submit(state, poseStack, collector, camera);
    }

    @Override
    protected int getModelTint(final CitizenRenderState state)
    {
        final int tint = super.getModelTint(state);
        return state.ghost ? ARGB.color(77, tint) : tint;
    }

    @Override
    protected @Nullable RenderType getRenderType(final CitizenRenderState state, final boolean visible, final boolean translucent, final boolean glowing)
    {
        if (state.ghost && visible)
        {
            return net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(getTextureLocation(state));
        }
        return super.getRenderType(state, visible, translucent, glowing);
    }

    @Override
    protected void submitNameTag(final CitizenRenderState state, final PoseStack poseStack, final SubmitNodeCollector collector, final CameraRenderState camera)
    {
        super.submitNameTag(state, poseStack, collector, camera);

        if (state.statusIcon != null && state.nameTagAttachment != null && state.distanceToCameraSq <= 4096.0D)
        {
            poseStack.pushPose();
            poseStack.translate(state.nameTagAttachment.x, state.nameTagAttachment.y + 0.9, state.nameTagAttachment.z);
            poseStack.mulPose(camera.orientation);
            poseStack.scale(0.025F, -0.025F, 0.025F);
            collector.submitCustomGeometry(poseStack, RenderTypes.worldEntityIcon(state.statusIcon), (pose, r) -> {
                final Matrix4f matrix = pose.pose();
                r.addVertex(matrix, -5, 0, 0).setUv(0, 0);
                r.addVertex(matrix, -5, 10, 0).setUv(0, 1);
                r.addVertex(matrix, 5, 10, 0).setUv(1, 1);
                r.addVertex(matrix, 5, 0, 0).setUv(1, 0);
            });
            poseStack.popPose();
        }
    }

    @NotNull
    @Override
    public Identifier getTextureLocation(final CitizenRenderState state)
    {
        return state.texture;
    }
}
