package me.dustin.jex.module.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventVisualCooldown;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;

@ModClass(name = "NoVisualCooldown", category = ModCategory.VISUAL, description = "Removes the visual effect of your weapon lowering after attacking.")
public class NoVisualCooldown extends Module {

    @EventListener(events = {EventVisualCooldown.class})
    private void runMethod(EventVisualCooldown eventVisualCooldown) {
        eventVisualCooldown.cancel();
    }
}
