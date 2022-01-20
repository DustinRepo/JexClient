package me.dustin.jex.feature.mod.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ApplyFogFilter;
import me.dustin.jex.event.render.EventApplyFog;
import me.dustin.jex.event.render.EventRenderOverlay;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.client.render.CameraSubmersionType;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Remove overlays")
public class AntiOverlay extends Feature {

    @Op(name = "Water")
    public boolean water = true;
    @Op(name = "Lava")
    public boolean lava = true;
    @Op(name = "Fire")
    public boolean fire = true;
    @Op(name = "Powder Snow")
    public boolean powderSnow = true;
    @Op(name = "In-Wall")
    public boolean inwall = true;
    @Op(name = "Spyglass")
    public boolean spyglass = true;
    @Op(name = "Pumpkin")
    public boolean pumpkin = true;
    @Op(name = "Portal")
    public boolean portal = true;
    @Op(name = "Vignette")
    public boolean vignette = true;

    @EventPointer
    private final EventListener<EventRenderOverlay> eventRenderOverlayEventListener = new EventListener<>(event -> {
        switch (event.getOverlay()) {
            case FIRE -> {
                if (fire)
                    event.cancel();
            }
            case UNDERWATER -> {
                if (water)
                    event.cancel();
            }
            case LAVA -> {
                if (lava)
                    event.cancel();
            }
            case IN_WALL -> {
                if (inwall)
                    event.cancel();
            }
            case PUMPKIN -> {
                if (pumpkin)
                    event.cancel();
            }
            case PORTAL -> {
                if (portal)
                    event.cancel();
            }
            case VIGNETTE -> {
                if (vignette)
                    event.cancel();
            }
            case COLD -> {
                if (powderSnow)
                    event.cancel();
            }
            case SPYGLASS -> {
                if (spyglass)
                    event.cancel();
            }
        }
    });

    @EventPointer
    private final EventListener<EventApplyFog> eventApplyFogEventListener = new EventListener<>(event -> {
        switch (event.getCameraSubmersionType()){
            case WATER -> {
                if (water) {
                    RenderSystem.setShaderFogStart(0);
                    RenderSystem.setShaderFogEnd(10000);
                }
            }
            case LAVA -> {
                if (lava) {
                    RenderSystem.setShaderFogStart(0);
                    RenderSystem.setShaderFogEnd(10000);
                }
            }
            case POWDER_SNOW -> {
                if (powderSnow) {
                    RenderSystem.setShaderFogStart(0);
                    RenderSystem.setShaderFogEnd(10000);
                }
            }
        }
    }, new ApplyFogFilter(CameraSubmersionType.WATER, CameraSubmersionType.LAVA, CameraSubmersionType.POWDER_SNOW));
}
