package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.entity.LivingEntity;

public class EventShouldFlipUpsideDown extends Event {
    private final LivingEntity livingEntity;
    private boolean flip;

    public EventShouldFlipUpsideDown(LivingEntity livingEntity, boolean flip) {
        this.livingEntity = livingEntity;
        this.flip = flip;
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    public boolean isFlip() {
        return flip;
    }

    public void setFlip(boolean flip) {
        this.flip = flip;
    }
}
