package me.dustin.jex.feature.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRenderOverlay;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;

@Feat(name = "AntiOverlay", category = FeatureCategory.VISUAL, description = "Remove overlays")
public class AntiOverlay extends Feature {

    @Op(name = "Water")
    public boolean water = true;
    @Op(name = "Lava")
    public boolean lava = true;
    @Op(name = "Fire")
    public boolean fire = true;
    @Op(name = "In-Wall")
    public boolean inwall = true;
    @Op(name = "Pumpkin")
    public boolean pumpkin = true;
    @Op(name = "Portal")
    public boolean portal = true;
    @Op(name = "Vignette")
    public boolean vignette = true;

    @EventListener(events = {EventRenderOverlay.class})
    private void runMethod(EventRenderOverlay eventRenderOverlay) {
        switch (eventRenderOverlay.getOverlay()) {
            case FIRE:
                if (fire)
                    eventRenderOverlay.cancel();
                break;
            case UNDERWATER:
                if (water)
                    eventRenderOverlay.cancel();
                break;
            case LAVA:
                if (lava)
                    eventRenderOverlay.cancel();
                break;
            case IN_WALL:
                if (inwall)
                    eventRenderOverlay.cancel();
                break;
            case PUMPKIN:
                if (pumpkin)
                    eventRenderOverlay.cancel();
                break;
            case PORTAL:
                if (portal)
                    eventRenderOverlay.cancel();
                break;
            case VIGNETTE:
                if (vignette)
                    eventRenderOverlay.cancel();
                break;
        }
    }
}
