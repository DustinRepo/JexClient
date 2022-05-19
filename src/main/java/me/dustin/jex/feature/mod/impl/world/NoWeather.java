package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.TickParticleFilter;
import me.dustin.jex.event.render.EventRenderRain;
import me.dustin.jex.event.world.EventTickParticle;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.client.particle.RainSplashParticle;

public class NoWeather extends Feature {

    public NoWeather() {
        super(Category.WORLD, "Remove rain and snow");
    }

    @EventPointer
    private final EventListener<EventRenderRain> eventRenderRainEventListener = new EventListener<>(event -> event.cancel());

    @EventPointer
    private final EventListener<EventTickParticle> eventTickParticleEventListener = new EventListener<>(event -> { event.cancel(); }, new TickParticleFilter(RainSplashParticle.class));
}
