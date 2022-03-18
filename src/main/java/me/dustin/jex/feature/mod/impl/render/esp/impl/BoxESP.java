package me.dustin.jex.feature.mod.impl.render.esp.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.feature.mod.impl.render.esp.ESP;
import net.minecraft.util.math.Vec3d;

public class BoxESP extends FeatureExtension {

    public BoxESP() {
        super("Box", ESP.class);
    }

    @Override
    public void pass(Event event1) {
        if (!event1.equals(EventRender3D.class))
            return;
        EventRender3D event = (EventRender3D) event1;

        Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
            if (ESP.INSTANCE.isValid(entity)) {
                try {
                    Vec3d vec = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, event.getPartialTicks());
                    Render3DHelper.INSTANCE.drawEntityBox(event.getMatrixStack(), entity, vec.getX(), vec.getY(), vec.getZ(), ESP.INSTANCE.getColor(entity));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
