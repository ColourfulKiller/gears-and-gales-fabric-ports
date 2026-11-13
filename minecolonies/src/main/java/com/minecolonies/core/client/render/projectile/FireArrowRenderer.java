package com.minecolonies.core.client.render.projectile;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.jetbrains.annotations.NotNull;

/**
 * Renders the animated magical fire arrow (Fabric 1.21.11: render-state based; the frame comes from the state's age).
 */
public class FireArrowRenderer extends ArrowRenderer<AbstractArrow, ArrowRenderState>
{
    private static final Identifier[] RES = new Identifier[] {
        Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/item/magicalarrows/magical_arrow1.png"),
        Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/item/magicalarrows/magical_arrow2.png"),
        Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/item/magicalarrows/magical_arrow3.png"),
        Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/item/magicalarrows/magical_arrow4.png"),
        Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/item/magicalarrows/magical_arrow5.png"),
        Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/item/magicalarrows/magical_arrow6.png")
    };

    public FireArrowRenderer(final EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public ArrowRenderState createRenderState()
    {
        return new ArrowRenderState();
    }

    @NotNull
    @Override
    protected Identifier getTextureLocation(@NotNull final ArrowRenderState state)
    {
        return RES[Math.floorMod((int) state.ageInTicks, 6)];
    }
}