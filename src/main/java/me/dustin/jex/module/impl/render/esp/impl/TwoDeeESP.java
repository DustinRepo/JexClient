package me.dustin.jex.module.impl.render.esp.impl;

import com.google.common.collect.Maps;
import me.dustin.events.core.Event;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.extension.ModuleExtension;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.module.impl.render.esp.ESP;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;

public class TwoDeeESP extends ModuleExtension {
    public TwoDeeESP() {
        super("2D", ESP.class);
    }


    private HashMap<Entity, Vec3d> headPos = Maps.newHashMap();
    private HashMap<Entity, Vec3d> footPos = Maps.newHashMap();

    @Override
    public void pass(Event event) {
        if (event instanceof EventRender3D) {
            EventRender3D eventRender3D = (EventRender3D)event;
            headPos.clear();
            footPos.clear();
            for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                if (ESP.INSTANCE.isValid(entity)) {
                    headPos.put(entity, Render2DHelper.INSTANCE.getPos(entity, entity.getHeight() + 0.2f, eventRender3D.getPartialTicks()));
                    footPos.put(entity, Render2DHelper.INSTANCE.getPos(entity, -0.2f, eventRender3D.getPartialTicks()));
                }
            }
        } else if (event instanceof EventRender2D) {
            EventRender2D eventRender2D = (EventRender2D)event;
            headPos.keySet().forEach(entity -> {
                Vec3d top = headPos.get(entity);
                Vec3d bottom = footPos.get(entity);
                if (Render2DHelper.INSTANCE.isOnScreen(top) && Render2DHelper.INSTANCE.isOnScreen(bottom)) {
                    float x = (float) top.x;
                    float y = (float) top.y;
                    float x2 = (float) bottom.x;
                    float y2 = (float) bottom.y;
                    if (y > y2) {
                        float saved = y;
                        y = y2;
                        y2 = saved;
                    }
                    if (x > x2) {
                        float saved = x;
                        x = x2;
                        x2 = saved;
                    }
                    float dif = Math.abs(y2 - y);

                    if (entity instanceof ItemEntity)
                        dif /= 2;
                    else
                        dif /= ClientMathHelper.INSTANCE.clamp(entity.getWidth() * 5f, 1f, 10f);
                    drawBox(eventRender2D.getMatrixStack(), x - dif, y + 1, x2 + dif, y2, entity);
                }
            });
        }
    }

    public void drawBox(MatrixStack matrixStack, float x, float y, float x2, float y2, Entity entity) {
        float f = 1f;

        if(entity instanceof LivingEntity){
            float percent = ((LivingEntity) entity).getHealth() / ((LivingEntity) entity).getMaxHealth();
            int color = Render2DHelper.INSTANCE.getPercentColor(percent * 100);
            float distance = y - y2;
            float pos = percent * distance;
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack,x2 - 1, y2 + pos, x2 + 2, y2, 0xff000000, color, 1);
        }
        int color = ESP.INSTANCE.getColor(entity) & 0x35ffffff;
        Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x, y, x2, y2, 0xff000000, color, 1);
    }

}
