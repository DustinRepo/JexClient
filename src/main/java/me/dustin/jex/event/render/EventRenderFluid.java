package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.block.Block;

public class EventRenderFluid extends Event {

    private Block block;

    public EventRenderFluid(Block block) {
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }
}
