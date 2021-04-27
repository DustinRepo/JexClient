package me.dustin.jex.event.world;

import me.dustin.events.core.Event;
import net.minecraft.client.particle.Particle;

public class EventTickParticle extends Event {

    private Particle particle;

    public EventTickParticle(Particle particle) {
        this.particle = particle;
    }

    public Particle getParticle() {
        return particle;
    }

    public enum Type {
        FIREWORK, EXPLOSION, SMOKE
    }
}
