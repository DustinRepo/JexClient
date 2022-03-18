package me.dustin.jex.event.render;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;

public class EventRender2D extends Event {

    private MatrixStack matrixStack;

    public EventRender2D(MatrixStack matrixStack) {
        this.matrixStack = matrixStack;
    }

    public MatrixStack getMatrixStack() {
        return this.matrixStack;
    }

}
