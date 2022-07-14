package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.render.BufferBuilder;

public class EventRenderWithShader extends Event {
    private final BufferBuilder.BuiltBuffer buffer;

    public EventRenderWithShader(BufferBuilder.BuiltBuffer buffer) {
        this.buffer = buffer;
    }

    public BufferBuilder.BuiltBuffer getBuffer() {
        return buffer;
    }
}
