package me.dustin.jex.module.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.client.util.InputUtil;

@ModClass(name = "Zoom", category = ModCategory.VISUAL, description = "Zoom in like optifine")
public class Zoom extends Module {

    @Op(name = "Zoom Level", min = 1, max = 5, inc = 0.1f)
    public float zoomLevel = 1;
    @Op(name = "On CTRL only")
    public boolean onCTRLOnly = false;

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

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            if((InputUtil.isKeyPressed(Wrapper.INSTANCE.getWindow().getHandle(), 341) || !onCTRLOnly) && Wrapper.INSTANCE.getMinecraft().currentScreen == null) {
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
        }
    }

}
