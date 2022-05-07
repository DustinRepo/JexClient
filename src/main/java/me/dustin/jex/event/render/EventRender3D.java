package me.dustin.jex.event.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.events.core.Event;

public class EventRender3D extends Event {

    private final float partialTicks;
    private final PoseStack poseStack;

    public EventRender3D(PoseStack poseStack, float partialTicks2) {
        this.partialTicks = partialTicks2;
        this.poseStack = poseStack;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public static class EventRender3DNoBob extends EventRender3D {

        public EventRender3DNoBob(PoseStack poseStack, float partialTicks2) {
            super(poseStack, partialTicks2);
        }
    }
}
