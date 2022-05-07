package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.renderer.ShaderInstance;

public class EventGetTranslucentShader extends Event {

    private ShaderInstance shader;

    public EventGetTranslucentShader(ShaderInstance shader) {
        this.shader = shader;
    }

    public ShaderInstance getShader() {
        return shader;
    }

    public void setShader(ShaderInstance shader) {
        this.shader = shader;
    }
}
