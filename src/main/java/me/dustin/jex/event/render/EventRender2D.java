package me.dustin.jex.event.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.events.core.Event;

public class EventRender2D extends Event {

    private final PoseStack poseStack;

    public EventRender2D(PoseStack poseStack) {
        this.poseStack = poseStack;
    }

    public PoseStack getPoseStack() {
        return this.poseStack;
    }

}
