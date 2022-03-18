package me.dustin.jex.feature.mod.impl.render.esp.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.render.EventHasOutline;
import me.dustin.jex.event.render.EventOutlineColor;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.impl.render.esp.ESP;
import me.dustin.jex.helper.misc.Wrapper;

public class ShaderESP extends FeatureExtension {

    public static boolean isOutlining;

    public ShaderESP() {
        super("Shader", ESP.class);
    }

    @Override
    public void pass(Event event) {
        if (event instanceof EventHasOutline eventHasOutline) {
            eventHasOutline.setOutline(ESP.INSTANCE.isValid(eventHasOutline.getEntity()));
        }
        if (event instanceof EventOutlineColor eventOutlineColor) {
            eventOutlineColor.setColor(ESP.INSTANCE.getColor(eventOutlineColor.getEntity()));
        }
    }

    @Override
    public void enable() {
        if (Wrapper.INSTANCE.getMinecraft().worldRenderer != null)
            Wrapper.INSTANCE.getMinecraft().worldRenderer.loadEntityOutlineShader();
        super.enable();
    }

    @Override
    public void disable() {
        if (Wrapper.INSTANCE.getMinecraft().worldRenderer != null)
            Wrapper.INSTANCE.getMinecraft().worldRenderer.loadEntityOutlineShader();

        super.disable();
    }
}
