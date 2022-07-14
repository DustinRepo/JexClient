package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;

public class EventWorldRender extends Event {

    private final float partialTicks;
    private final MatrixStack poseStack;

    private final Mode mode;

    public EventWorldRender(MatrixStack poseStack, float partialTicks2, Mode mode) {
        this.partialTicks = partialTicks2;
        this.poseStack = poseStack;
        this.mode = mode;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public MatrixStack getPoseStack() {
        return poseStack;
    }

    public Mode getMode() {
        return mode;
    }

    public enum Mode {
        PRE, POST
    }
}
