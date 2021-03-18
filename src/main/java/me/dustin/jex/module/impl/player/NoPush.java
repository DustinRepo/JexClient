package me.dustin.jex.module.impl.player;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPushAwayFromEntity;
import me.dustin.jex.event.world.EventWaterVelocity;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;

@ModClass(name = "NoPush", category = ModCategory.PLAYER, description = "Don't let others push you around.")
public class NoPush extends Module {

    @Op(name = "Mobs")
    public boolean mobs = true;
    @Op(name = "Water")
    public boolean water = true;

    @EventListener(events = {EventWaterVelocity.class, EventPushAwayFromEntity.class})
    private void runMethod(Event event) {
        if (water && event instanceof EventWaterVelocity)
            event.cancel();
        if (mobs && event instanceof EventPushAwayFromEntity)
            event.cancel();
    }
}
