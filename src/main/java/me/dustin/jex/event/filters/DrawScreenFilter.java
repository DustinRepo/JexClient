package me.dustin.jex.event.filters;

import me.dustin.jex.event.render.EventDrawScreen;
import net.minecraft.client.gui.screens.Screen;
import java.util.function.Predicate;

public class DrawScreenFilter implements Predicate<EventDrawScreen> {

    private final EventDrawScreen.Mode mode;
    private final Class<? extends Screen>[] screens;

    @SafeVarargs
    public DrawScreenFilter(EventDrawScreen.Mode mode, Class<? extends Screen>... screens) {
        this.mode = mode;
        this.screens = screens;
    }

    @Override
    public boolean test(EventDrawScreen eventDrawScreen) {
        if (screens.length <= 0)
            if (mode != null)
                return mode == eventDrawScreen.getMode();
            else
                return true;
        for (Class<? extends Screen> screen : screens) {
            if (screen == eventDrawScreen.getScreen().getClass()) {
                if (mode != null)
                    return mode == eventDrawScreen.getMode();
                return true;
            }
        }
        return false;
    }
}
