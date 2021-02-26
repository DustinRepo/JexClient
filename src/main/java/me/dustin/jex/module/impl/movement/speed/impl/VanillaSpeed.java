package me.dustin.jex.module.impl.movement.speed.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.extension.ModuleExtension;
import me.dustin.jex.helper.misc.BaritoneHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.impl.movement.speed.Speed;

public class VanillaSpeed extends ModuleExtension {

    private boolean slowdown = false;

    public VanillaSpeed() {
        super("Vanilla", Speed.class);
    }

    @Override
    public void pass(Event event) {
        if (event instanceof EventMove) {
            if (BaritoneHelper.INSTANCE.isBaritoneRunning() && !Wrapper.INSTANCE.getLocalPlayer().isOnGround())
                return;
            PlayerHelper.INSTANCE.setMoveSpeed((EventMove) event, ((Speed) Module.get(Speed.class)).vanillaSpeed);
        }
    }

}
