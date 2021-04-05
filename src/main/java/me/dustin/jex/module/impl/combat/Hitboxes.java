package me.dustin.jex.module.impl.combat;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventEntityHitbox;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;

@ModClass(name = "Hitboxes", category = ModCategory.COMBAT, description = "Resize entity hitboxes to make them easier to hit")
public class Hitboxes extends Module {

    @Op(name = "Expansion", min = 0.5f, max = 2f, inc = 0.1f)
    public float expansion = 0.5f;

    @EventListener(events = {EventEntityHitbox.class})
    public void runMethod(EventEntityHitbox eventEntityHitbox) {
        if (eventEntityHitbox.getEntity() == null || eventEntityHitbox.getEntity().getId() == Wrapper.INSTANCE.getLocalPlayer().getId())
            return;
        eventEntityHitbox.setBox(eventEntityHitbox.getBox().expand(expansion, 0, expansion));
    }

}
