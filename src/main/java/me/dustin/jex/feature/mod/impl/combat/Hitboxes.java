package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventEntityHitbox;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;

public class Hitboxes extends Feature {

    @Op(name = "Expansion", min = 0.5f, max = 2f, inc = 0.1f)
    public float expansion = 0.5f;

    public Hitboxes() {
        super(Category.COMBAT, "Resize entity hitboxes to make them easier to hit");
    }

    @EventPointer
    private final EventListener<EventEntityHitbox> eventEntityHitboxEventListener = new EventListener<>(event -> {
        if (event.getEntity() == null || event.getEntity().getId() == Wrapper.INSTANCE.getLocalPlayer().getId())
            return;
        event.setBox(event.getBox().expand(expansion, 0, expansion));
    });
}
