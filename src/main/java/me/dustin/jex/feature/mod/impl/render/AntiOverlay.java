package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRenderOverlay;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Remove overlays")
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

    @EventPointer
    private final EventListener<EventRenderOverlay> eventRenderOverlayEventListener = new EventListener<>(event -> {
        switch (event.getOverlay()) {
            case FIRE:
                if (fire)
                    event.cancel();
                break;
            case UNDERWATER:
                if (water)
                    event.cancel();
                break;
            case LAVA:
                if (lava)
                    event.cancel();
                break;
            case IN_WALL:
                if (inwall)
                    event.cancel();
                break;
            case PUMPKIN:
                if (pumpkin)
                    event.cancel();
                break;
            case PORTAL:
                if (portal)
                    event.cancel();
                break;
            case VIGNETTE:
                if (vignette)
                    event.cancel();
                break;
        }
    });
}
