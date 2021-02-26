package me.dustin.jex.event.render;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.events.core.Event;
import net.minecraft.block.entity.BlockEntity;

public class EventRenderBlockEntity extends Event {

    public BlockEntity blockEntity;

    public EventRenderBlockEntity(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

}
