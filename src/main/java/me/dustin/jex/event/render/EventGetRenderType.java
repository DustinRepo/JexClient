package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;

public class EventGetRenderType extends Event {
    private final BlockState state;
    private RenderLayer renderType;

    public EventGetRenderType(BlockState state, RenderLayer renderType) {
        this.state = state;
        this.renderType = renderType;
    }

    public BlockState getState() {
        return state;
    }

    public RenderLayer getRenderType() {
        return renderType;
    }

    public void setRenderType(RenderLayer renderType) {
        this.renderType = renderType;
    }
}
