package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventGetFramerateLimit;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.Wrapper;

public class UnfocusedFPS extends Feature {

    @Op(name = "Unfocused FPS", min = 1, max = 25)
    public int unfocusedFPS = 15;

    public UnfocusedFPS() {
        super(Category.MISC, "Limit the FPS while Minecraft isn't in focus to save resources");
        setState(true);
    }

    @EventPointer
    private final EventListener<EventGetFramerateLimit> eventGetFramerateLimitEventListener = new EventListener<>(event -> {
        boolean focused = Wrapper.INSTANCE.getMinecraft().isWindowFocused();
        if (!focused) {
            event.setLimit(unfocusedFPS);
        }
    });
}
