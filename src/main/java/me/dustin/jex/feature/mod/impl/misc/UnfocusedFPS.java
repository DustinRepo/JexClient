package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventGetFramerateLimit;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.Wrapper;

@Feature.Manifest(description = "Limit the FPS while Minecraft isn't in focus to save resources", category = Feature.Category.MISC, enabled = true)
public class UnfocusedFPS extends Feature {

    @Op(name = "Unfocused FPS", min = 1, max = 25)
    public int unfocusedFPS = 15;

    @EventListener(events = {EventGetFramerateLimit.class})
    private void runMethod(EventGetFramerateLimit eventGetFramerateLimit) {
        boolean focused = Wrapper.INSTANCE.getMinecraft().isWindowFocused();
        if (!focused) {
            eventGetFramerateLimit.setLimit(unfocusedFPS);
        }
    }
}
