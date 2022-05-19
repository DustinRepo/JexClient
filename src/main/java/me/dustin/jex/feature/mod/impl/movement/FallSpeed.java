package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.impl.movement.fly.Fly;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.player.Freecam;
import me.dustin.jex.feature.option.annotate.Op;

public class FallSpeed extends Feature {

    @Op(name = "Fall Distance", min = 0, max = 10, inc = 0.5f)
    public float fallDistance = 3;

    @Op(name = "Speed", min = 0.1f, max = 15f, inc = 0.1f)
    public float speed = 0.5f;

    public FallSpeed() {
        super(Category.MOVEMENT, "Change your fall speed");
    }

    @EventPointer
    private final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
        if (Feature.getState(Fly.class) || Feature.getState(Freecam.class))
            return;
        if (Wrapper.INSTANCE.getLocalPlayer().fallDistance > fallDistance && !Wrapper.INSTANCE.getLocalPlayer().isOnGround()) {
            event.setY(-speed);
        }
    });
}
