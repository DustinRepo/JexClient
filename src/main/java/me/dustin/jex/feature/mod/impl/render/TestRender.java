package me.dustin.jex.feature.mod.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.math.Matrix4x4;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.VertexObjectList;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.awt.*;

@Feature.Manifest(name = "TestRender", category = Feature.Category.VISUAL, description = "Dev shit. Shouldn't be in a release")
public class TestRender extends Feature {

    @EventListener(events = {EventRender3D.class})
    private void runMethod(EventRender3D eventRender3D) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
        RenderSystem.enableCull();

        Color color1 = ColorHelper.INSTANCE.getColor(ColorHelper.INSTANCE.getClientColor());
        Color color2 = ColorHelper.INSTANCE.getColor(ColorHelper.INSTANCE.getClientColor() & 0x70ffffff);
        Matrix4f matrix4f = eventRender3D.getMatrixStack().peek().getModel();

        double d = Wrapper.INSTANCE.getIGameRenderer().getFOV(eventRender3D.getPartialTicks());
        Matrix4f projection = Wrapper.INSTANCE.getGameRenderer().getBasicProjectionMatrix(d);
        ShaderHelper.INSTANCE.setProjectionMatrix(Matrix4x4.copyFromColumnMajor(projection));
        ShaderHelper.INSTANCE.setModelViewMatrix(Matrix4x4.copyFromColumnMajor(RenderSystem.getModelViewMatrix()));

        ShaderHelper.INSTANCE.getPosColorShader().bind();
        VertexObjectList vertexObjectList = VertexObjectList.getMain();
        vertexObjectList.begin(VertexObjectList.DrawMode.LINE, VertexObjectList.Format.POS_COLOR);

        Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
            if (entity instanceof LivingEntity && !(entity instanceof ClientPlayerEntity)) {
                Vec3d vec3d = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, eventRender3D.getPartialTicks());
                Box bb = new Box(vec3d.x - entity.getWidth() + 0.25, vec3d.y, vec3d.z - entity.getWidth() + 0.25, vec3d.x + entity.getWidth() - 0.25, vec3d.y + entity.getHeight() + 0.1, vec3d.z + entity.getWidth() - 0.25);
                VoxelShape shape = VoxelShapes.cuboid(bb);
                shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
                    vertexObjectList.vertex(matrix4f, (float)x1, (float)y1, (float)z1).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha());
                    vertexObjectList.vertex(matrix4f, (float)x2, (float)y2, (float)z2).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha());
                });
            }
        });
        vertexObjectList.end();
        vertexObjectList.draw();


        Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
            if (entity instanceof LivingEntity && !(entity instanceof ClientPlayerEntity)) {
                Vec3d vec3d = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, eventRender3D.getPartialTicks());
                Box bb = new Box(vec3d.x - entity.getWidth() + 0.25, vec3d.y, vec3d.z - entity.getWidth() + 0.25, vec3d.x + entity.getWidth() - 0.25, vec3d.y + entity.getHeight() + 0.1, vec3d.z + entity.getWidth() - 0.25);
                float minX = (float)bb.minX;
                float minY = (float)bb.minY;
                float minZ = (float)bb.minZ;
                float maxX = (float)bb.maxX;
                float maxY = (float)bb.maxY;
                float maxZ = (float)bb.maxZ;

                vertexObjectList.begin(VertexObjectList.DrawMode.QUAD, VertexObjectList.Format.POS_COLOR);
                /*0*/vertexObjectList.vertex(matrix4f, maxX, minY, maxZ).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha());
                /*1*/vertexObjectList.vertex(matrix4f, minX, minY, maxZ).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha());
                /*2*/vertexObjectList.vertex(matrix4f, minX, minY, minZ).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha());
                /*3*/vertexObjectList.vertex(matrix4f, maxX, minY, minZ).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha());
                /*4*/vertexObjectList.vertex(matrix4f, maxX, maxY, maxZ).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha());
                /*5*/vertexObjectList.vertex(matrix4f, minX, maxY, maxZ).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha());
                /*6*/vertexObjectList.vertex(matrix4f, minX, maxY, minZ).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha());
                /*7*/vertexObjectList.vertex(matrix4f, maxX, maxY, minZ).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha());
                vertexObjectList.index(0, 1, 2).index(2, 3, 0);//bottom
                vertexObjectList.index(0, 3, 7).index(7, 4, 0);//east face
                vertexObjectList.index(0, 4, 5).index(5, 1, 0);//south face
                vertexObjectList.index(2, 1, 5).index(5, 6, 2);//west face
                vertexObjectList.index(2, 6, 7).index(7, 3, 2);//north face
                vertexObjectList.index(4, 7, 6).index(6, 5, 4);//top
                vertexObjectList.end();
                vertexObjectList.draw();
            }
        });

        ShaderHelper.INSTANCE.getPosColorShader().detach();
        Render3DHelper.INSTANCE.end3DRender();
    }

}
