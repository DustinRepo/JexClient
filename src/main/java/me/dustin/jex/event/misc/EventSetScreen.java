package me.dustin.jex.event.misc;

import me.dustin.events.core.Event;
import net.minecraft.client.gui.screens.Screen;

public class EventSetScreen extends Event {

    private Screen screen;

    public EventSetScreen(Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }


}
