package me.dustin.jex.feature.impl.combat;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventEntityHitbox;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;

@Feat(name = "Hitboxes", category = FeatureCategory.COMBAT, description = "Resize entity hitboxes to make them easier to hit")
public class Hitboxes extends Feature {

    @Op(name = "Expansion", min = 0.5f, max = 2f, inc = 0.1f)
    public float expansion = 0.5f;

    @EventListener(events = {EventEntityHitbox.class})
    public void runMethod(EventEntityHitbox eventEntityHitbox) {
        if (eventEntityHitbox.getEntity() == null || eventEntityHitbox.getEntity().getEntityId() == Wrapper.INSTANCE.getLocalPlayer().getEntityId())
            return;
        eventEntityHitbox.setBox(eventEntityHitbox.getBox().expand(expansion, 0, expansion));
    }

}
