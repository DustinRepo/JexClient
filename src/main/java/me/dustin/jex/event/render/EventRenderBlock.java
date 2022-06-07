package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.block.Block;

public class EventRenderBlock extends Event {

    public Block block;

    public EventRenderBlock(Block block) {
        this.block = block;
    }

}
