package me.dustin.jex.event.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.events.core.Event;

public class EventWorldRender extends Event {

    private final float partialTicks;
    private final PoseStack poseStack;

    public EventWorldRender(PoseStack poseStack, float partialTicks2) {
        this.partialTicks = partialTicks2;
        this.poseStack = poseStack;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }
}
