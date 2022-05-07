package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EventRenderBlockEntity extends Event {

    public BlockEntity blockEntity;

    public EventRenderBlockEntity(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

}
