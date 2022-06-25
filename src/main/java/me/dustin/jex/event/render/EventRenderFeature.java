package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.entity.Entity;

public class EventRenderFeature extends Event {
    private final FeatureRenderer<?, ?> featureRenderer;
    private final Entity entity;

    public EventRenderFeature(FeatureRenderer<?, ?> featureRenderer, Entity entity) {
        this.featureRenderer = featureRenderer;
        this.entity = entity;
    }

    public FeatureRenderer<?, ?> getFeatureRenderer() {
        return featureRenderer;
    }

    public Entity getEntity() {
        return entity;
    }
}
