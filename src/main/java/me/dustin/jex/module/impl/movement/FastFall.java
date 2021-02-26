package me.dustin.jex.module.impl.movement;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.module.impl.player.Freecam;
import me.dustin.jex.option.annotate.Op;

@ModClass(name = "FallSpeed", category = ModCategory.MOVEMENT, description = "Fall faster")
public class FastFall extends Module {

    @Op(name = "Fall Distance", min = 0, max = 10, inc = 0.5f)
    public float fallDistance = 3;

    @Op(name = "Speed", min = 0.1f, max = 15f, inc = 0.1f)
    public float speed = 0.5f;

    @EventListener(events = {EventMove.class})
    private void runMethod(EventMove eventMove) {
        if (Module.get(Fly.class).getState() || Module.get(Freecam.class).getState())
            return;
        if (Wrapper.INSTANCE.getLocalPlayer().fallDistance > fallDistance && !Wrapper.INSTANCE.getLocalPlayer().isOnGround()) {
            eventMove.setY(-speed);
        }
    }

}
