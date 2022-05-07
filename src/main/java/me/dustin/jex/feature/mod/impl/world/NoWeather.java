package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.TickParticleFilter;
import me.dustin.jex.event.render.EventRenderRain;
import me.dustin.jex.event.world.EventTickParticle;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.client.particle.WaterDropParticle;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Remove rain and snow")
public class NoWeather extends Feature {
    @EventPointer
    private final EventListener<EventRenderRain> eventRenderRainEventListener = new EventListener<>(event -> event.cancel());

    @EventPointer
    private final EventListener<EventTickParticle> eventTickParticleEventListener = new EventListener<>(event -> { event.cancel(); }, new TickParticleFilter(WaterDropParticle.class));
}
