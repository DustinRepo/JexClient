package me.dustin.jex.helper.render.shader.post.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.BufferHelper;
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
        this.getSecond().clear(MinecraftClient.IS_SYSTEM_MAC);
        this.getSecond().beginWrite(false);
        RenderSystem.depthFunc(519);
        BufferBuilder bufferBuilder = BufferHelper.INSTANCE.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        bufferBuilder.vertex(0.0, 0.0, 500.0).next();
        bufferBuilder.vertex(f, 0.0, 500.0).next();
        bufferBuilder.vertex(f, g, 500.0).next();
        bufferBuilder.vertex(0.0, g, 500.0).next();
        BufferHelper.INSTANCE.drawWithShader(bufferBuilder, this.getShader());
        RenderSystem.depthFunc(515);
        this.getSecond().endWrite();
        this.getFirst().endRead();
        Wrapper.INSTANCE.getMinecraft().getFramebuffer().beginWrite(true);
    }
}
