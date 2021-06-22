package me.dustin.jex.event.render;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.events.core.Event;

public class EventBlockBrightness extends Event {

    private int brightness;

    public EventBlockBrightness(int brightness) {
        this.brightness = brightness;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }
}
