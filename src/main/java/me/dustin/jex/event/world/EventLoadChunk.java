package me.dustin.jex.event.world;

import me.dustin.events.core.Event;
import net.minecraft.world.level.chunk.LevelChunk;

public class EventLoadChunk extends Event {

    private final LevelChunk worldChunk;

    public EventLoadChunk(LevelChunk worldChunk) {
        this.worldChunk = worldChunk;
    }

    public LevelChunk getWorldChunk() {
        return worldChunk;
    }
}
