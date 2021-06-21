package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;

public class EventGetRenderLayer extends Event {
    private BlockState state;
    private RenderLayer renderLayer;

    public EventGetRenderLayer(BlockState state) {
        this.state = state;
    }

    public BlockState getState() {
        return state;
    }

    public RenderLayer getRenderLayer() {
        return renderLayer;
    }

    public void setRenderLayer(RenderLayer renderLayer) {
        this.renderLayer = renderLayer;
    }
}
