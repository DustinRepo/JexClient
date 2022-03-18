package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;

public class EventRenderWidget extends Event {

    private ClickableWidget clickableWidget;
    private MatrixStack matrixStack;

    public EventRenderWidget(ClickableWidget clickableWidget, MatrixStack matrixStack) {
        this.clickableWidget = clickableWidget;
        this.matrixStack = matrixStack;
    }

    public ClickableWidget getClickableWidget() {
        return clickableWidget;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }
}
