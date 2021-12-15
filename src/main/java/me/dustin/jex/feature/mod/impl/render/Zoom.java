package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import org.lwjgl.glfw.GLFW;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Zoom in like Optifine")
public class Zoom extends Feature {

    @Op(name = "Zoom Level", min = 1, max = 5, inc = 0.1f)
    public float zoomLevel = 1;
    @Op(name = "Zoom Key", isKeybind = true)
    public int zoomKey = GLFW.GLFW_KEY_LEFT_CONTROL;

    private double savedFOV;
    boolean resetFOV = true;

    @Override
    public void onEnable() {
        if (Wrapper.INSTANCE.getOptions() != null)
            savedFOV = Wrapper.INSTANCE.getOptions().fov;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (Wrapper.INSTANCE.getOptions() == null)
            super.onDisable();
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if(KeyboardHelper.INSTANCE.isPressed(zoomKey) && Wrapper.INSTANCE.getMinecraft().currentScreen == null) {
            if(resetFOV)
            {
                this.resetFOV = false;
                this.savedFOV = (float)Wrapper.INSTANCE.getOptions().fov;
            }
            float zoomFov = 30 - (6 * zoomLevel);
            if (zoomFov == 0)
                zoomFov = 1;
            if(Wrapper.INSTANCE.getOptions().fov > zoomFov)
            {
                Wrapper.INSTANCE.getOptions().fov = zoomFov;
            }
        }
        else
        {
            if(!resetFOV || !getState()) {
                if (Wrapper.INSTANCE.getOptions().fov < savedFOV) {
                    Wrapper.INSTANCE.getOptions().fov = savedFOV;
                }
                if (!getState())
                    super.onDisable();
            } else
                this.resetFOV = true;
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
