package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;

public class EventRenderGetPos extends Event {

    private float partialTicks;
    private MatrixStack matrixStack;

    public EventRenderGetPos(MatrixStack matrixStack, float partialTicks2) {
        this.partialTicks = partialTicks2;
        this.matrixStack = matrixStack;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }
}
