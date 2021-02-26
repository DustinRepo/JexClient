package me.dustin.jex.module.impl.player;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerUpdates;
import me.dustin.jex.event.world.EventWaterVelocity;
import me.dustin.jex.helper.misc.Wrapper;
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

    private float savedPush = -999;

    @EventListener(events = {EventPlayerUpdates.class})
    private void runEvent(EventPlayerUpdates eventPlayerUpdates) {
        if (eventPlayerUpdates.getMode() == EventPlayerUpdates.Mode.PRE) {

            if (mobs) {
                if (savedPush == -999)
                    savedPush = Wrapper.INSTANCE.getLocalPlayer().pushSpeedReduction;

                Wrapper.INSTANCE.getLocalPlayer().pushSpeedReduction = 1;
            } else if (savedPush != -999) {
                Wrapper.INSTANCE.getLocalPlayer().pushSpeedReduction = savedPush;
                savedPush = -999;
            }
        }
    }

    @EventListener(events = {EventWaterVelocity.class})
    private void runMethod(EventWaterVelocity eventWaterVelocity) {
        if (water)
            eventWaterVelocity.cancel();
    }

    @Override
    public void onDisable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null && savedPush != -999) {
            Wrapper.INSTANCE.getLocalPlayer().pushSpeedReduction = savedPush;
            savedPush = -999;
        }
        super.onDisable();
    }

}
