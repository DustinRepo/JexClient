package me.dustin.jex.feature.mod.impl.render.esp.impl;

import java.util.HashMap;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;

import me.dustin.events.core.Event;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.impl.render.esp.ESP;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public class TwoDeeESP extends FeatureExtension {
    public TwoDeeESP() {
        super("2D", ESP.class);
    }


    private HashMap<Entity, Vec3d> headPos = Maps.newHashMap();
    private HashMap<Entity, Vec3d> footPos = Maps.newHashMap();

    @Override
    public void pass(Event event) {
        if (event instanceof EventRender3D eventRender3D) {
            headPos.clear();
            footPos.clear();
            for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                if (ESP.INSTANCE.isValid(entity)) {
                    headPos.put(entity, Render2DHelper.INSTANCE.getPos(entity, entity.getHeight() + 0.2f, eventRender3D.getPartialTicks(), eventRender3D.getMatrixStack()));
                    footPos.put(entity, Render2DHelper.INSTANCE.getPos(entity, -0.2f, eventRender3D.getPartialTicks(), eventRender3D.getMatrixStack()));
                }
            }
        } else if (event instanceof EventRender2D) {
            EventRender2D eventRender2D = (EventRender2D)event;
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            Render2DHelper.INSTANCE.setup2DRender(true);
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
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
            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);
            bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
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
                    outlineBox(eventRender2D.getMatrixStack(), x - dif, y + 1, x2 + dif, y2, entity);
                }
            });
            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);
            
            Render2DHelper.INSTANCE.end2DRender();
        }
    }

    public void drawBox(MatrixStack matrixStack, float x, float y, float x2, float y2, Entity entity) {
        int color = ESP.INSTANCE.getColor(entity) & 0x50ffffff;
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        float f = (float)(color >> 24 & 255) / 255.0F;
        float g = (float)(color >> 16 & 255) / 255.0F;
        float h = (float)(color >> 8 & 255) / 255.0F;
        float k = (float)(color & 255) / 255.0F;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.vertex(matrix, (float)x, (float)y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (float)x2, (float)y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (float)x2, (float)y, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (float)x, (float)y, 0.0F).color(g, h, k, f).next();
    }
    
    public void outlineBox(MatrixStack matrixStack, float x, float y, float x2, float y2, Entity entity) {
        int color = 0xff000000;
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        float f = (float)(color >> 24 & 255) / 255.0F;
        float g = (float)(color >> 16 & 255) / 255.0F;
        float h = (float)(color >> 8 & 255) / 255.0F;
        float k = (float)(color & 255) / 255.0F;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.vertex(matrix, (float)x, (float)y, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (float)x, (float)y2, 0.0F).color(g, h, k, f).next();

        bufferBuilder.vertex(matrix, (float)x, (float)y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (float)x2, (float)y2, 0.0F).color(g, h, k, f).next();

        bufferBuilder.vertex(matrix, (float)x2, (float)y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (float)x2, (float)y, 0.0F).color(g, h, k, f).next();
        
        bufferBuilder.vertex(matrix, (float)x2, (float)y, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (float)x, (float)y, 0.0F).color(g, h, k, f).next();
    }

}
