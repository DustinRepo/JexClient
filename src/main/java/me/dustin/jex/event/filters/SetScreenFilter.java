package me.dustin.jex.event.filters;

import me.dustin.jex.event.misc.EventSetScreen;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.Predicate;

public class SetScreenFilter implements Predicate<EventSetScreen> {

    private final Class<? extends Screen>[] screens;

    @SafeVarargs
    public SetScreenFilter(Class<? extends Screen>... screens) {
        this.screens = screens;
    }

    @Override
    public boolean test(EventSetScreen eventSetScreen) {
        if (screens.length <= 0)
            return true;
        for (Class<? extends Screen> screen : screens) {
            if (eventSetScreen.getScreen() == null)
                return screen == null;
            if (screen == eventSetScreen.getScreen().getClass()) {
                return true;
            }
        }
        return false;
    }
}
