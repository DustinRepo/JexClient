package me.dustin.jex.feature.mod.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRenderBackground;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.VertexObjectList;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;

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
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        VertexObjectList vertexObjectList = new VertexObjectList(GL11.GL_TRIANGLE_STRIP);
        float x = 0;
        float y = 0;
        float width = Render2DHelper.INSTANCE.getScaledWidth();
        float height = Render2DHelper.INSTANCE.getScaledHeight();

        x = x - width;//really hacky fix for the proj matrix not changing origin from middle of screen rather than top-right like I want it
        y = y - height;
        width *= 2.f;
        height *= 2.f;
        Render2DHelper.INSTANCE.testShader.bind();
        if (Wrapper.INSTANCE.getLocalPlayer() == null) {
            vertexObjectList.vertex(matrix4f,x, y + height, 0).color(0.5f, 0.5f, 0.5f, 1);
            vertexObjectList.vertex(matrix4f,x,y, 0).color(0.5f, 0.5f, 0.5f, 1);
            vertexObjectList.vertex(matrix4f,x + width, y + height, 0).color(0.5f, 0.5f, 0.5f, 1);
            vertexObjectList.vertex(matrix4f,x + width,y, 0).color(0.5f, 0.5f, 0.5f, 1);
        }
        vertexObjectList.vertex(matrix4f,x, y + height, 0).color(bottomLeft.getRed() / 255.f, bottomLeft.getGreen() / 255.f, bottomLeft.getBlue() / 255.f, 0.5f - a);
        vertexObjectList.vertex(matrix4f,x,y, 0).color(topLeft.getRed() / 255.f, topLeft.getGreen() / 255.f, topLeft.getBlue() / 255.f, a + 0.3f);
        vertexObjectList.vertex(matrix4f,x + width, y + height, 0).color(bottomRight.getRed() / 255.f, bottomRight.getGreen() / 255.f, bottomRight.getBlue() / 255.f, a + 0.3f);
        vertexObjectList.vertex(matrix4f,x + width,y, 0).color(topRight.getRed() / 255.f, topRight.getGreen() / 255.f, topRight.getBlue() / 255.f, 0.5f - a);
        vertexObjectList.end();
        VertexObjectList.draw(vertexObjectList);
        Render2DHelper.INSTANCE.testShader.detach();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();

        //for testing with MC's coordinates
        /*float g=1,h=0,k=0,f=1;
        vertexObjectList.vertex(matrix4f, (float)x, (float)y + height, 0.0F).color(g, h, k, f);
        vertexObjectList.vertex(matrix4f, (float)x + width, (float)y + height, 0.0F).color(g, h, k, f);
        vertexObjectList.vertex(matrix4f, (float)x + width, (float)y, 0.0F).color(g, h, k, f);
        vertexObjectList.vertex(matrix4f, (float)x, (float)y, 0.0F).color(g, h, k, f);*/
    }

}
