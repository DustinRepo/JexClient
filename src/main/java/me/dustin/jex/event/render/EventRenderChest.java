package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;

public class EventRenderChest extends Event {

    private boolean christmas;
    private Mode mode;

    public EventRenderChest(MatrixStack matrixStack, Mode mode, boolean christmas) {
        this.christmas = christmas;
        this.mode = mode;
    }

    public boolean isChristmas() {
        return christmas;
    }

    public void setChristmas(boolean christmas) {
        this.christmas = christmas;
    }

    public Mode getMode() {
        return mode;
    }

    public enum Mode {
        PRE, POST
    }
}
