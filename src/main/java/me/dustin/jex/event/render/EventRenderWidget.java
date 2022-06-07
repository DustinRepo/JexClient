package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;

public class EventRenderWidget extends Event {

    private final ClickableWidget abstractWidget;
    private final MatrixStack poseStack;

    public EventRenderWidget(ClickableWidget abstractWidget, MatrixStack poseStack) {
        this.abstractWidget = abstractWidget;
        this.poseStack = poseStack;
    }

    public ClickableWidget getAbstractWidget() {
        return abstractWidget;
    }

    public MatrixStack getPoseStack() {
        return poseStack;
    }
}
