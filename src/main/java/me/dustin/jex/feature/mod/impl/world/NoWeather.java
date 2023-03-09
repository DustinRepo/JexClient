package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.filters.SoundFilter;
import me.dustin.jex.event.filters.TickParticleFilter;
import me.dustin.jex.event.render.EventRenderRain;
import me.dustin.jex.event.world.EventPlaySound;
import me.dustin.jex.event.world.EventTickParticle;
import me.dustin.jex.event.world.EventWeatherGradient;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.client.particle.RainSplashParticle;
import net.minecraft.util.Identifier;

public class NoWeather extends Feature {

    public NoWeather() {
        super(Category.WORLD);
    }

    @EventPointer
    private final EventListener<EventRenderRain> eventRenderRainEventListener = new EventListener<>(event -> event.cancel());

    @EventPointer
    private final EventListener<EventWeatherGradient> eventWeatherGradientEventListener = new EventListener<>(event -> {
       event.setWeatherGradient(0);
       event.cancel();
    });

    @EventPointer
    private final EventListener<EventPlaySound> eventPlaySoundEventListener = new EventListener<>(event -> {
        event.cancel();
    }, new SoundFilter(EventPlaySound.Mode.PRE, new Identifier("weather.rain"), new Identifier("entity.lightning_bolt.thunder"), new Identifier("entity.lightning_bolt.impact")));

    @EventPointer
    private final EventListener<EventTickParticle> eventTickParticleEventListener = new EventListener<>(event -> {
        event.cancel();
    }, new TickParticleFilter(RainSplashParticle.class));
}
