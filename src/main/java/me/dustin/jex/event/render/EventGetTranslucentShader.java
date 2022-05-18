package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.render.Shader;

public class EventGetTranslucentShader extends Event {

    private Shader shader;

    public EventGetTranslucentShader(Shader shader) {
        this.shader = shader;
    }

    public Shader getShader() {
        return shader;
    }

    public void setShader(Shader shader) {
        this.shader = shader;
    }
}
