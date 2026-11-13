package com.minecolonies.core.client.render.mobs.drownedpirates;

import com.minecolonies.api.client.render.modeltype.RaiderRenderState;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMonster;
import com.minecolonies.core.client.render.mobs.AbstractRaiderRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Abstract for rendering DrownedPirate raiders (Fabric 1.21.11: everything shared lives in AbstractRaiderRenderer).
 */
public abstract class AbstractRendererDrownedPirate<T extends AbstractEntityMinecoloniesMonster, M extends HumanoidModel<RaiderRenderState>> extends AbstractRaiderRenderer<T, M>
{
    public AbstractRendererDrownedPirate(final EntityRendererProvider.Context context, final M modelBipedIn, final float shadowSize)
    {
        super(context, modelBipedIn, shadowSize);
    }
}
