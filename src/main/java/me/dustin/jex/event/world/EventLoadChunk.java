package me.dustin.jex.event.world;

import me.dustin.events.core.Event;
import net.minecraft.world.chunk.WorldChunk;

public class EventLoadChunk extends Event {

    private WorldChunk worldChunk;

    public EventLoadChunk(WorldChunk worldChunk) {
        this.worldChunk = worldChunk;
    }

    public WorldChunk getWorldChunk() {
        return worldChunk;
    }
}
