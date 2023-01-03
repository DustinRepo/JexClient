package me.dustin.jex.feature.mod.impl.movement.fly.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.impl.movement.fly.Fly;
import me.dustin.jex.helper.misc.Wrapper;

public class CreativeFly extends FeatureExtension {

    public CreativeFly() {
        super(Fly.Mode.CREATIVE, Fly.class);
    }

    @Override
    public void pass(Event event) {
        if (event instanceof EventPlayerPackets eventPlayerPackets && eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
	boolean jumping = Wrapper.INSTANCE.getOptions().jumpKey.isPressed();
        if (jumping) {
            Wrapper.INSTANCE.getLocalPlayer().jump();
}    
}
}
}
