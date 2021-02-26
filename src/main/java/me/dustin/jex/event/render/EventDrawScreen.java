package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

public class EventDrawScreen extends Event {

    private Screen screen;
    private MatrixStack matrixStack;
    private Mode mode;

    public EventDrawScreen(Screen screen, MatrixStack matrixStack, Mode mode) {
        this.screen = screen;
        this.matrixStack = matrixStack;
        this.mode = mode;
    }

    public Screen getScreen() {
        return screen;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public Mode getMode() {
        return mode;
    }

    public enum Mode {
        PRE, POST, POST_CONTAINER
    }
}
