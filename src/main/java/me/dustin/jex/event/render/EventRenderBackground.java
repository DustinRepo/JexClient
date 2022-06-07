package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;

public class EventRenderBackground extends Event {

    private final MatrixStack poseStack;

    public EventRenderBackground(MatrixStack poseStack) {
        this.poseStack = poseStack;
    }

    public MatrixStack getPoseStack() {
        return poseStack;
    }
}
