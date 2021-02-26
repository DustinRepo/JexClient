package me.dustin.jex.event.render;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.events.core.Event;
import net.minecraft.block.Block;

public class EventBlockBrightness extends Event {

    private Block block;
    private int brightness;

    public EventBlockBrightness(Block block, int brightness) {
        this.block = block;
        this.brightness = brightness;
    }

    public Block getBlock() {
        return block;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }
}
