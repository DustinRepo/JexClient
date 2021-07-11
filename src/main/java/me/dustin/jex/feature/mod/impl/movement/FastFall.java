package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.player.Freecam;
import me.dustin.jex.feature.option.annotate.Op;

@Feature.Manifest(name = "FallSpeed", category = Feature.Category.MOVEMENT, description = "Fall faster")
public class FastFall extends Feature {

    @Op(name = "Fall Distance", min = 0, max = 10, inc = 0.5f)
    public float fallDistance = 3;

    @Op(name = "Speed", min = 0.1f, max = 15f, inc = 0.1f)
    public float speed = 0.5f;

    @EventListener(events = {EventMove.class})
    private void runMethod(EventMove eventMove) {
        if (Feature.get(Fly.class).getState() || Feature.get(Freecam.class).getState())
            return;
        if (Wrapper.INSTANCE.getLocalPlayer().fallDistance > fallDistance && !Wrapper.INSTANCE.getLocalPlayer().isOnGround()) {
            eventMove.setY(-speed);
        }
    }

}
