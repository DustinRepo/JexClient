package me.dustin.jex.module.impl.render.esp.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.Event;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.extension.ModuleExtension;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.Stencil;
import me.dustin.jex.module.impl.render.esp.ESP;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class OutlineBox extends ModuleExtension {

    public OutlineBox() {
        super("Box Outline", ESP.class);
    }

    @Override
    public void pass(Event event) {
        if (event instanceof EventRender3D) {
            EventRender3D eventRender3D = (EventRender3D) event;

            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
            RenderSystem.enableCull();

            Stencil.INSTANCE.write();
            Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                if (ESP.INSTANCE.isValid(entity)) {
                    Vec3d vec = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, eventRender3D.getPartialTicks());
                    Box bb = new Box(vec.x - entity.getWidth() + 0.25, vec.y, vec.z - entity.getWidth() + 0.25, vec.x + entity.getWidth() - 0.25, vec.y + entity.getHeight() + 0.1, vec.z + entity.getWidth() - 0.25);
                    if (entity instanceof ItemEntity)
                        bb = new Box(vec.x - 0.15, vec.y + 0.1f, vec.z - 0.15, vec.x + 0.15, vec.y + 0.5, vec.z + 0.15);
                    Render3DHelper.INSTANCE.drawFilledBox(eventRender3D.getMatrixStack(), bb, ESP.INSTANCE.getColor(entity));
                }
            });
            RenderSystem.lineWidth(ESP.INSTANCE.lineWidth);
            Stencil.INSTANCE.erase();
            RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

            Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                if (ESP.INSTANCE.isValid(entity)) {
                    Vec3d vec = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, eventRender3D.getPartialTicks());
                    Box bb = new Box(vec.x - entity.getWidth() + 0.25, vec.y, vec.z - entity.getWidth() + 0.25, vec.x + entity.getWidth() - 0.25, vec.y + entity.getHeight() + 0.1, vec.z + entity.getWidth() - 0.25);
                    if (entity instanceof ItemEntity)
                        bb = new Box(vec.x - 0.15, vec.y + 0.1f, vec.z - 0.15, vec.x + 0.15, vec.y + 0.5, vec.z + 0.15);
                    Render3DHelper.INSTANCE.drawOutlineBox(eventRender3D.getMatrixStack(), bb, ESP.INSTANCE.getColor(entity));
                }
            });

            Stencil.INSTANCE.dispose();
            GL11.glPopAttrib();
            RenderSystem.enableTexture();
            RenderSystem.disableCull();
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
        }
    }
}
