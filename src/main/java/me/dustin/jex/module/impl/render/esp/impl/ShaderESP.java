package me.dustin.jex.module.impl.render.esp.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.render.EventOutlineColor;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.extension.ModuleExtension;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.impl.render.esp.ESP;

public class ShaderESP extends ModuleExtension {

    public static boolean isOutlining;

    public ShaderESP() {
        super("Shader", ESP.class);
    }

    @Override
    public void pass(Event event) {
        ESP esp = (ESP) Module.get(ESP.class);
        if (event instanceof EventRender3D) {
            EventRender3D eventRender3D = (EventRender3D) event;
            Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                entity.setGlowing(esp.isValid(entity));
            });
        }
        if (event instanceof EventOutlineColor) {
            EventOutlineColor eventOutlineColor = (EventOutlineColor) event;
            eventOutlineColor.setColor(esp.getColor(eventOutlineColor.getEntity()));
        }
        /*if (event instanceof EventJoinWorld) {
            if (Wrapper.INSTANCE.getMinecraft().worldRenderer != null)
                Wrapper.INSTANCE.getMinecraft().worldRenderer.loadEntityOutlineShader();
        }*/
    }

    @Override
    public void enable() {
        try {
            if (Wrapper.INSTANCE.getMinecraft().worldRenderer != null)
                Wrapper.INSTANCE.getMinecraft().worldRenderer.loadEntityOutlineShader();
        } catch (Exception e) {
            System.out.println("Loading");
        }
        super.enable();
    }

    @Override
    public void disable() {
        if (Wrapper.INSTANCE.getWorld() != null)
            Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                entity.setGlowing(false);
            });
        if (Wrapper.INSTANCE.getMinecraft().worldRenderer != null)
            Wrapper.INSTANCE.getMinecraft().worldRenderer.loadEntityOutlineShader();

        super.disable();
    }
}
