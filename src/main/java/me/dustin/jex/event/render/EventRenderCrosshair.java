package me.dustin.jex.event.render;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;

public class EventRenderCrosshair extends Event {

    private MatrixStack matrixStack;

    public EventRenderCrosshair(MatrixStack matrixStack) {
        this.matrixStack = matrixStack;
    }

    public MatrixStack getMatrixStack() {
        return this.matrixStack;
    }

}
