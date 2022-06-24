package me.dustin.jex.feature.mod.impl.render.esp.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.render.EventHasOutline;
import me.dustin.jex.event.render.EventTeamColor;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.impl.render.esp.ESP;
import me.dustin.jex.helper.misc.Wrapper;

public class ShaderESP extends FeatureExtension {

    public static boolean isOutlining;

    public ShaderESP() {
        super(ESP.Mode.SHADER, ESP.class);
    }

    @Override
    public void pass(Event event) {
        if (event instanceof EventHasOutline eventHasOutline) {
            eventHasOutline.setOutline(ESP.INSTANCE.isValid(eventHasOutline.getEntity()));
        }
        if (event instanceof EventTeamColor eventTeamColor) {
            eventTeamColor.setColor(ESP.INSTANCE.getColor(eventTeamColor.getEntity()));
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
