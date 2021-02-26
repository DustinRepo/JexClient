package me.dustin.jex.module.impl.render.esp.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.extension.ModuleExtension;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.Stencil;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.impl.render.esp.ESP;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class OutlineBox extends ModuleExtension {

    public OutlineBox() {
        super("Box Outline", ESP.class);
    }

    @Override
    public void pass(Event event) {
        if (event instanceof EventRender3D) {
            EventRender3D eventRender3D = (EventRender3D) event;
            ESP esp = (ESP) Module.get(ESP.class);

            glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            glEnable(GL_LINE_SMOOTH);
            glDisable(GL_TEXTURE_2D);
            glEnable(GL_CULL_FACE);
            glDisable(GL_DEPTH_TEST);
            glDisable(GL_LIGHTING);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL_TEXTURE_2D);

            Stencil.INSTANCE.write();
            Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                if (esp.isValid(entity)) {
                    Vec3d vec = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, eventRender3D.getPartialTicks());
                    Box bb = new Box(vec.x - entity.getWidth() + 0.25, vec.y, vec.z - entity.getWidth() + 0.25, vec.x + entity.getWidth() - 0.25, vec.y + entity.getHeight() + 0.1, vec.z + entity.getWidth() - 0.25);
                    if (entity instanceof ItemEntity)
                        bb = new Box(vec.x - 0.15, vec.y + 0.1f, vec.z - 0.15, vec.x + 0.15, vec.y + 0.5, vec.z + 0.15);

                    Render3DHelper.INSTANCE.drawFilledBox(bb);
                }
            });
            GL11.glLineWidth(esp.lineWidth);
            Stencil.INSTANCE.erase();
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

            Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                if (esp.isValid(entity)) {
                    Vec3d vec = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, eventRender3D.getPartialTicks());
                    Box bb = new Box(vec.x - entity.getWidth() + 0.25, vec.y, vec.z - entity.getWidth() + 0.25, vec.x + entity.getWidth() - 0.25, vec.y + entity.getHeight() + 0.1, vec.z + entity.getWidth() - 0.25);
                    if (entity instanceof ItemEntity)
                        bb = new Box(vec.x - 0.15, vec.y + 0.1f, vec.z - 0.15, vec.x + 0.15, vec.y + 0.5, vec.z + 0.15);
                    Render2DHelper.INSTANCE.glColor(esp.getColor(entity));
                    Render3DHelper.INSTANCE.drawOutlineBox(bb);
                }
            });

            Stencil.INSTANCE.dispose();
            glEnable(GL_DEPTH_TEST);
            glEnable(GL_TEXTURE_2D);
            glDisable(GL_LINE_SMOOTH);
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
    }
}
