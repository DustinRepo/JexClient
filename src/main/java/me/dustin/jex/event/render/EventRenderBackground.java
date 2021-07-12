package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;

public class EventRenderBackground extends Event {

    private MatrixStack matrixStack;

    public EventRenderBackground(MatrixStack matrixStack) {
        this.matrixStack = matrixStack;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }
}
