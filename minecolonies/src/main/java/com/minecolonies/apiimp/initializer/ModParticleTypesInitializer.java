package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.particles.SleepingParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.fabricmc.api.EnvType;
import com.ldtteam.fabric.event.bus.SubscribeEvent;
import com.ldtteam.fabric.event.client.RegisterParticleProvidersEvent;
import com.ldtteam.fabric.event.registry.RegisterEvent;

/**
 * Initializes the particle type.
 */
public class ModParticleTypesInitializer
{
    /**
     * Particle type
     */
    public static final SimpleParticleType SLEEPINGPARTICLE_TYPE = net.fabricmc.fabric.api.particle.v1.FabricParticleTypes.simple(true);
    public static final Identifier  SLEEPING_TEXTURE      = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "particle/sleeping");

    /**
     * Register the particle
     */
    public static class CommonRegistration
    {
        @SubscribeEvent
        public static void registerParticles(final RegisterEvent event)
        {
            event.register(Registries.PARTICLE_TYPE, SLEEPING_TEXTURE, () -> SLEEPINGPARTICLE_TYPE);
        }
    }

    /**
     * Register the client side factory
     */
    public static class ClientRegistration
    {
        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event)
        {
            event.registerSpriteSet(SLEEPINGPARTICLE_TYPE, SleepingParticle.Factory::new);
        }
    }
}
