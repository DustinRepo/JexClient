package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;

public class EventRenderCrosshair extends Event {

    private final MatrixStack poseStack;

    public EventRenderCrosshair(MatrixStack poseStack) {
        this.poseStack = poseStack;
    }

    public MatrixStack getPoseStack() {
        return this.poseStack;
    }

}
