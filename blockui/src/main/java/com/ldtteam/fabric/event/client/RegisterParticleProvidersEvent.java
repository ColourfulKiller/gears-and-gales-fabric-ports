package com.ldtteam.fabric.event.client;

import com.ldtteam.fabric.event.bus.Event;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

import java.util.function.Function;

/** NeoForge's RegisterParticleProvidersEvent: straight to Fabric's particle factory registry. */
public class RegisterParticleProvidersEvent extends Event
{
    public <T extends ParticleOptions> void registerSpriteSet(final ParticleType<T> type, final Function<SpriteSet, ParticleProvider<T>> registration)
    {
        ParticleFactoryRegistry.getInstance().register(type, registration::apply);
    }

    public <T extends ParticleOptions> void registerSpecial(final ParticleType<T> type, final ParticleProvider<T> provider)
    {
        ParticleFactoryRegistry.getInstance().register(type, provider);
    }
}