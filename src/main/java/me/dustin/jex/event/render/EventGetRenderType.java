package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;

public class EventGetRenderType extends Event {
    private final BlockState state;
    private RenderType renderType;

    public EventGetRenderType(BlockState state, RenderType renderType) {
        this.state = state;
        this.renderType = renderType;
    }

    public BlockState getState() {
        return state;
    }

    public RenderType getRenderType() {
        return renderType;
    }

    public void setRenderType(RenderType renderType) {
        this.renderType = renderType;
    }
}
