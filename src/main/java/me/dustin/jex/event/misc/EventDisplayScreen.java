package me.dustin.jex.event.misc;

import me.dustin.events.core.Event;
import net.minecraft.client.gui.screen.Screen;

public class EventDisplayScreen extends Event {

    private Screen screen;

    public EventDisplayScreen(Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }


}
