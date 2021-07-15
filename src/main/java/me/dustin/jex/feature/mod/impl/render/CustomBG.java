package me.dustin.jex.feature.mod.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRenderBackground;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.math.Matrix4x4;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.VertexObjectList;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import net.minecraft.util.math.Matrix4f;

import java.awt.*;

@Feature.Manifest(name = "CustomBG", category = Feature.Category.VISUAL, description = "Draws a custom background rather than the simple dark one")
public class CustomBG extends Feature {

    @Op(name = "InGame Only")
    public boolean inGameOnly = true;

    float offset = 0;
    float a = 0.49f;
    boolean up = false;
    private Timer timer = new Timer();

    @EventListener(events = {EventRenderBackground.class})
    private void runMethod(EventRenderBackground eventRenderBackground) {
        if (inGameOnly && Wrapper.INSTANCE.getLocalPlayer() == null)
            return;
        eventRenderBackground.cancel();
        Matrix4f matrix4f = eventRenderBackground.getMatrixStack().peek().getModel();
        if (timer.hasPassed(20)) {
            if (up) {
                if (a < .49f)
                    a+=0.005f;
                else
                    up = false;
            } else {
                if (a > 0.01f)
                    a-=0.005f;
                else
                    up = true;
            }
            offset += 0.25f;
            if (offset > 270)
                offset -=270;
            timer.reset();
        }
        float topLeftColor = offset;
        float topRightColor = offset + 80;
        float bottomRightColor = offset + (80 * 2);
        float bottomLeftColor = offset + (80 * 3);
        if (topRightColor > 270)
            topRightColor-=270;
        if (bottomRightColor > 270)
            bottomRightColor-=270;
        if (bottomLeftColor > 270)
            bottomLeftColor-=270;

        Color topLeft = ColorHelper.INSTANCE.getColorViaHue(topLeftColor);
        Color topRight = ColorHelper.INSTANCE.getColorViaHue(topRightColor);
        Color bottomRight = ColorHelper.INSTANCE.getColorViaHue(bottomRightColor);
        Color bottomLeft = ColorHelper.INSTANCE.getColorViaHue(bottomLeftColor);

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        //little test square
        /*Render2DHelper.INSTANCE.testShader.bind();
        vertexObjectList.begin(VertexObjectList.DrawMode.QUAD);
        vertexObjectList.vertex(matrix4f, 100, 0, 0).color(1, 1, 0, 1);//top right
        vertexObjectList.vertex(matrix4f, 0, 0, 0).color(1, 0, 0, 1);//top left
        vertexObjectList.vertex(matrix4f, 100, 100, 0).color(0, 1, 0, 1);//bottom right
        vertexObjectList.vertex(matrix4f, 0, 100, 0).color(0, 0, 1, 1);//bottom left
        vertexObjectList.end();
        vertexObjectList.draw();
        Render2DHelper.INSTANCE.testShader.detach();*/

        Matrix4x4 ortho = Matrix4x4.ortho2DMatrix(0, Render2DHelper.INSTANCE.getScaledWidth(), Render2DHelper.INSTANCE.getScaledHeight(), 0, -0.1f, 1000.f);
        ShaderHelper.INSTANCE.setProjectionMatrix(ortho);
        ShaderHelper.INSTANCE.setModelViewMatrix(Matrix4x4.copyFromRowMajor(RenderSystem.getModelViewMatrix()));
        float x = 0;
        float y = 0;
        float width = Render2DHelper.INSTANCE.getScaledWidth();
        float height = Render2DHelper.INSTANCE.getScaledHeight();

        VertexObjectList vertexObjectList = VertexObjectList.getMain();
        ShaderHelper.INSTANCE.getPosColorShader().bind();
        if (Wrapper.INSTANCE.getLocalPlayer() == null) {
            vertexObjectList.begin(VertexObjectList.DrawMode.QUAD, VertexObjectList.Format.POS_COLOR);
            vertexObjectList.vertex(matrix4f,x + width,y, 0).color(0.5f, 0.5f, 0.5f, 1);
            vertexObjectList.vertex(matrix4f,x,y, 0).color(0.5f, 0.5f, 0.5f, 1);
            vertexObjectList.vertex(matrix4f,x + width, y + height, 0).color(0.5f, 0.5f, 0.5f, 1);
            vertexObjectList.vertex(matrix4f,x, y + height, 0).color(0.5f, 0.5f, 0.5f, 1);
            //vertexObjectList.index(0,1,3).index(3,1,2);
            vertexObjectList.end();
            vertexObjectList.draw();
        }
        //if using .index, switch last two .vertex

        vertexObjectList.begin(VertexObjectList.DrawMode.QUAD, VertexObjectList.Format.POS_COLOR);
        vertexObjectList.vertex(matrix4f,x + width,y, 0).color(topRight.getRed() / 255.f, topRight.getGreen() / 255.f, topRight.getBlue() / 255.f, 0.5f - a);
        vertexObjectList.vertex(matrix4f,x,y, 0).color(topLeft.getRed() / 255.f, topLeft.getGreen() / 255.f, topLeft.getBlue() / 255.f, a + 0.3f);
        vertexObjectList.vertex(matrix4f,x + width, y + height, 0).color(bottomRight.getRed() / 255.f, bottomRight.getGreen() / 255.f, bottomRight.getBlue() / 255.f, a + 0.3f);
        vertexObjectList.vertex(matrix4f,x, y + height, 0).color(bottomLeft.getRed() / 255.f, bottomLeft.getGreen() / 255.f, bottomLeft.getBlue() / 255.f, 0.5f - a);
        //vertexObjectList.index(0,1,3).index(3,1,2);
        vertexObjectList.end();
        vertexObjectList.draw();
        ShaderHelper.INSTANCE.getPosColorShader().detach();
        RenderSystem.enableTexture();
    }

}
