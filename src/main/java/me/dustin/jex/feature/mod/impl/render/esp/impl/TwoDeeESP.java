package me.dustin.jex.feature.mod.impl.render.esp.impl;

import java.util.HashMap;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import me.dustin.events.core.Event;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.impl.render.esp.ESP;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

public class TwoDeeESP extends FeatureExtension {
    public TwoDeeESP() {
        super("2D", ESP.class);
    }


    private HashMap<Entity, Vec3> headPos = Maps.newHashMap();
    private HashMap<Entity, Vec3> footPos = Maps.newHashMap();

    @Override
    public void pass(Event event) {
        if (event instanceof EventRender3D eventRender3D) {
            headPos.clear();
            footPos.clear();
            for (Entity entity : Wrapper.INSTANCE.getWorld().entitiesForRendering()) {
                if (ESP.INSTANCE.isValid(entity)) {
                    headPos.put(entity, Render2DHelper.INSTANCE.getPos(entity, entity.getBbHeight() + 0.2f, eventRender3D.getPartialTicks(), eventRender3D.getPoseStack()));
                    footPos.put(entity, Render2DHelper.INSTANCE.getPos(entity, -0.2f, eventRender3D.getPartialTicks(), eventRender3D.getPoseStack()));
                }
            }
        } else if (event instanceof EventRender2D eventRender2D) {
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            Render2DHelper.INSTANCE.setup2DRender(true);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            headPos.keySet().forEach(entity -> {
                Vec3 top = headPos.get(entity);
                Vec3 bottom = footPos.get(entity);
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
                        dif /= ClientMathHelper.INSTANCE.clamp(entity.getBbWidth() * 5f, 1f, 10f);
                    drawBox(eventRender2D.getPoseStack(), x - dif, y + 1, x2 + dif, y2, entity);
                }
            });
            bufferBuilder.clear();
            BufferUploader.drawWithShader(bufferBuilder.end());
            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            headPos.keySet().forEach(entity -> {
                Vec3 top = headPos.get(entity);
                Vec3 bottom = footPos.get(entity);
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
                        dif /= ClientMathHelper.INSTANCE.clamp(entity.getBbWidth() * 5f, 1f, 10f);
                    outlineBox(eventRender2D.getPoseStack(), x - dif, y + 1, x2 + dif, y2, entity);
                }
            });
            bufferBuilder.clear();
            BufferUploader.drawWithShader(bufferBuilder.end());
            Render2DHelper.INSTANCE.end2DRender();
        }
    }

    public void drawBox(PoseStack matrixStack, float x, float y, float x2, float y2, Entity entity) {
        int color = ESP.INSTANCE.getColor(entity) & 0x50ffffff;
        Matrix4f matrix = matrixStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        float f = (float)(color >> 24 & 255) / 255.0F;
        float g = (float)(color >> 16 & 255) / 255.0F;
        float h = (float)(color >> 8 & 255) / 255.0F;
        float k = (float)(color & 255) / 255.0F;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.vertex(matrix, (float)x, (float)y2, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, (float)x2, (float)y2, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, (float)x2, (float)y, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, (float)x, (float)y, 0.0F).color(g, h, k, f).endVertex();
    }
    
    public void outlineBox(PoseStack matrixStack, float x, float y, float x2, float y2, Entity entity) {
        int color = 0xff000000;
        Matrix4f matrix = matrixStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        float f = (float)(color >> 24 & 255) / 255.0F;
        float g = (float)(color >> 16 & 255) / 255.0F;
        float h = (float)(color >> 8 & 255) / 255.0F;
        float k = (float)(color & 255) / 255.0F;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.vertex(matrix, (float)x, (float)y, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, (float)x, (float)y2, 0.0F).color(g, h, k, f).endVertex();

        bufferBuilder.vertex(matrix, (float)x, (float)y2, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, (float)x2, (float)y2, 0.0F).color(g, h, k, f).endVertex();

        bufferBuilder.vertex(matrix, (float)x2, (float)y2, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, (float)x2, (float)y, 0.0F).color(g, h, k, f).endVertex();
        
        bufferBuilder.vertex(matrix, (float)x2, (float)y, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, (float)x, (float)y, 0.0F).color(g, h, k, f).endVertex();
    }

}
