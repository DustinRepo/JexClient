package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.render.Shader;

public class EventBindShader extends Event {
    private final Shader shader;
    private final Mode mode;

    public EventBindShader(Shader shader, Mode mode) {
        this.shader = shader;
        this.mode = mode;
    }

    public Shader getShader() {
        return shader;
    }

    public Mode getMode() {
        return mode;
    }

    public enum Mode {
        PRE, POST
    }
}
