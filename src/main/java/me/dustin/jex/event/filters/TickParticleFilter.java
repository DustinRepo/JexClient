package me.dustin.jex.event.filters;

import me.dustin.jex.event.misc.EventSetScreen;
import me.dustin.jex.event.world.EventTickParticle;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.particle.Particle;

import java.util.function.Predicate;

public class TickParticleFilter implements Predicate<EventTickParticle> {

    private final Class<? extends Particle>[] particles;

    @SafeVarargs
    public TickParticleFilter(Class<? extends Particle>... particles) {
        this.particles = particles;
    }

    @Override
    public boolean test(EventTickParticle eventTickParticle) {
        if (particles.length <= 0)
            return true;
        for (Class<? extends Particle> particle : particles) {
            if (eventTickParticle.getParticle() == null)
                return particle == null;
            if (particle == eventTickParticle.getParticle().getClass()) {
                return true;
            }
        }
        return false;
    }
}
