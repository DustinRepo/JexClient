package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.misc.EventSetOptionInstance;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import org.lwjgl.glfw.GLFW;

public class Zoom extends Feature {

    public final Property<Boolean> mouseSmoothProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Mouse Smooth")
            .description("Smooth the mouse movement while zoomed like Optifine.")
            .value(true)
            .build();
    public final Property<Float> zoomLevelProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Zoom Level")
            .value(1f)
            .min(1)
            .max(10)
            .value(1f)
            .inc(1f)
            .build();
    public final Property<Integer> zoomKeyProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Zoom Key")
            .value(GLFW.GLFW_KEY_LEFT_CONTROL)
            .isKey()
            .build();

    private int savedFOV;
    boolean resetFOV = true;

    public Zoom() {
        super(Category.VISUAL, "Zoom in like Optifine");
    }

    @Override
    public void onEnable() {
        if (Wrapper.INSTANCE.getOptions() != null)
            savedFOV = Wrapper.INSTANCE.getOptions().getFov().getValue();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (Wrapper.INSTANCE.getOptions() == null)
            super.onDisable();
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if(KeyboardHelper.INSTANCE.isPressed(zoomKeyProperty.value()) && Wrapper.INSTANCE.getMinecraft().currentScreen == null) {
            if(resetFOV) {
                this.resetFOV = false;
                this.savedFOV = Wrapper.INSTANCE.getOptions().getFov().getValue();
            }
            int zoomFov = (int)(30 - (2 * zoomLevelProperty.value()));
            if (zoomFov == 0)
                zoomFov = 30;
            if(Wrapper.INSTANCE.getOptions().getFov().getValue() > zoomFov) {
                Wrapper.INSTANCE.getOptions().getFov().setValue(zoomFov);
                if (mouseSmoothProperty.value())
                    Wrapper.INSTANCE.getOptions().smoothCameraEnabled = true;
            }
        }
        else
        {
            if(!resetFOV || !getState()) {
                if (Wrapper.INSTANCE.getOptions().getFov().getValue() < savedFOV) {
                    Wrapper.INSTANCE.getOptions().getFov().setValue(savedFOV);
                    if (mouseSmoothProperty.value())
                        Wrapper.INSTANCE.getOptions().smoothCameraEnabled = false;
                }
                if (!getState()) {
                    super.onDisable();
                    if (mouseSmoothProperty.value())
                        Wrapper.INSTANCE.getOptions().smoothCameraEnabled = false;
                }
            } else
                this.resetFOV = true;
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventSetOptionInstance> eventSetSimpleOptionEventListener = new EventListener<>(event -> {
        if (event.getOptionInstance() == Wrapper.INSTANCE.getOptions().getFov())
            event.setShouldIgnoreCheck(true);
    });
}
