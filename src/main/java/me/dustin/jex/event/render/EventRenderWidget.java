package me.dustin.jex.event.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.events.core.Event;
import net.minecraft.client.gui.components.AbstractWidget;

public class EventRenderWidget extends Event {

    private final AbstractWidget abstractWidget;
    private final PoseStack poseStack;

    public EventRenderWidget(AbstractWidget abstractWidget, PoseStack poseStack) {
        this.abstractWidget = abstractWidget;
        this.poseStack = poseStack;
    }

    public AbstractWidget getAbstractWidget() {
        return abstractWidget;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }
}
