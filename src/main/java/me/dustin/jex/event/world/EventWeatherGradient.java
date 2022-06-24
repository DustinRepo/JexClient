package me.dustin.jex.event.world;

import me.dustin.events.core.Event;

public class EventWeatherGradient extends Event {

    private float weatherGradient;

    public EventWeatherGradient(float weatherGradient) {
        this.weatherGradient = weatherGradient;
    }

    public float getWeatherGradient() {
        return weatherGradient;
    }

    public void setWeatherGradient(float weatherGradient) {
        this.weatherGradient = weatherGradient;
    }
}
