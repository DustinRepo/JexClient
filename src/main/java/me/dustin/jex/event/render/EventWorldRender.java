package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;

public class EventWorldRender extends Event {

    private final float partialTicks;
    private final MatrixStack poseStack;

    public EventWorldRender(MatrixStack poseStack, float partialTicks2) {
        this.partialTicks = partialTicks2;
        this.poseStack = poseStack;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public MatrixStack getPoseStack() {
        return poseStack;
    }
}
