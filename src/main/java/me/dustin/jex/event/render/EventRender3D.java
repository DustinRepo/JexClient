package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;

public class EventRender3D extends Event {

    private final float partialTicks;
    private final MatrixStack poseStack;

    public EventRender3D(MatrixStack poseStack, float partialTicks2) {
        this.partialTicks = partialTicks2;
        this.poseStack = poseStack;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public MatrixStack getPoseStack() {
        return poseStack;
    }

    public static class EventRender3DNoBob extends EventRender3D {

        public EventRender3DNoBob(MatrixStack poseStack, float partialTicks2) {
            super(poseStack, partialTicks2);
        }
    }
}
