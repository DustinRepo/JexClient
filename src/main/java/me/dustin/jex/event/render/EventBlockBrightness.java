package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.world.level.block.Block;

public class EventBlockBrightness extends Event {

    private final Block block;
    private int brightness;

    public EventBlockBrightness(Block block, int brightness) {
        this.block = block;
        this.brightness = brightness;
    }

    public int getBrightness() {
        return brightness;
    }

    public Block getBlock() {
        return this.block;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }
}
