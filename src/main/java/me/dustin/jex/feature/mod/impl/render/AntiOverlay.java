package me.dustin.jex.feature.mod.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ApplyFogFilter;
import me.dustin.jex.event.render.EventSetupFog;
import me.dustin.jex.event.render.EventRenderOverlay;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import net.minecraft.client.render.CameraSubmersionType;

public class AntiOverlay extends Feature {

    public final Property<Boolean> waterProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Water")
            .value(true)
            .build();
    public final Property<Boolean> lavaProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Lava")
            .value(true)
            .build();
    public final Property<Boolean> fireProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Fire")
            .value(true)
            .build();
    public final Property<Boolean> powderSnowProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Powder Snow")
            .value(true)
            .build();
    public final Property<Boolean> inwallProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Block")
            .value(true)
            .build();
    public final Property<Boolean> spyglassProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Spyglass")
            .value(true)
            .build();
    public final Property<Boolean> pumpkinProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Pumpkin")
            .value(true)
            .build();
    public final Property<Boolean> portalProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Portal")
            .value(true)
            .build();
    public final Property<Boolean> vignetteProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Vignette")
            .value(true)
            .build();
    public final Property<Boolean> scoreboardProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Scoreboard")
            .value(true)
            .build();
public final Property<Boolean> bossbarProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Bossbar")
            .value(true)
            .build();
    public AntiOverlay() {
        super(Category.VISUAL, "Remove overlays");
    }

    @EventPointer
    private final EventListener<EventRenderOverlay> eventRenderOverlayEventListener = new EventListener<>(event -> {
        switch (event.getOverlay()) {
            case BOSSBAR -> {
            if (bossbarProperty.value())
                    event.cancel();
            }
            case SCOREBOARD -> {
                if (scoreboardProperty.value())
                    event.cancel();
            }
            case FIRE -> {
                if (fireProperty.value())
                    event.cancel();
            }
            case UNDERWATER -> {
                if (waterProperty.value())
                    event.cancel();
            }
            case LAVA -> {
                if (lavaProperty.value())
                    event.cancel();
            }
            case IN_WALL -> {
                if (inwallProperty.value())
                    event.cancel();
            }
            case PUMPKIN -> {
                if (pumpkinProperty.value())
                    event.cancel();
            }
            case PORTAL -> {
                if (portalProperty.value())
                    event.cancel();
            }
            case VIGNETTE -> {
                if (vignetteProperty.value())
                    event.cancel();
            }
            case COLD -> {
                if (powderSnowProperty.value())
                    event.cancel();
            }
            case SPYGLASS -> {
                if (spyglassProperty.value())
                    event.cancel();
            }
        }
    });

    @EventPointer
    private final EventListener<EventSetupFog> eventApplyFogEventListener = new EventListener<>(event -> {
        switch (event.getCameraSubmersionType()){
            case WATER -> {
                if (waterProperty.value()) {
                    RenderSystem.setShaderFogStart(0);
                    RenderSystem.setShaderFogEnd(10000);
                }
            }
            case LAVA -> {
                if (lavaProperty.value()) {
                    RenderSystem.setShaderFogStart(0);
                    RenderSystem.setShaderFogEnd(10000);
                }
            }
            case POWDER_SNOW -> {
                if (powderSnowProperty.value()) {
                    RenderSystem.setShaderFogStart(0);
                    RenderSystem.setShaderFogEnd(10000);
                }
            }
        }
    }, new ApplyFogFilter(CameraSubmersionType.WATER, CameraSubmersionType.LAVA, CameraSubmersionType.POWDER_SNOW));
}
