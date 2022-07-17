package me.dustin.jex.helper.render.shader.post.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.helper.render.shader.post.PostProcessEffect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;

public class PostProcessOutline extends PostProcessEffect {
    public PostProcessOutline() {
        super(ShaderHelper.INSTANCE::getOutlineShader);
    }

    @Override
    public void render() {
        this.checkResize();
        this.getFirst().endWrite();
        this.getFirst().beginRead();
        float f = this.getSecond().textureWidth;
        float g = this.getSecond().textureHeight;
        RenderSystem.viewport(0, 0, (int)f, (int)g);
        this.getShader().bind();
        this.getSecond().clear(MinecraftClient.IS_SYSTEM_MAC);
        this.getSecond().beginWrite(false);
        RenderSystem.depthFunc(519);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        bufferBuilder.vertex(0.0, 0.0, 500.0).next();
        bufferBuilder.vertex(f, 0.0, 500.0).next();
        bufferBuilder.vertex(f, g, 500.0).next();
        bufferBuilder.vertex(0.0, g, 500.0).next();
        BufferRenderer.drawWithoutShader(bufferBuilder.end());
        RenderSystem.depthFunc(515);
        this.getShader().detach();
        this.getSecond().endWrite();
        this.getFirst().endRead();
        Wrapper.INSTANCE.getMinecraft().getFramebuffer().beginWrite(true);
    }
}
