package me.dustin.jex.helper.render.shader.post.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.math.Matrix4x4;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.helper.render.shader.impl.BlurShader;
import me.dustin.jex.helper.render.shader.post.PostProcessEffect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec2f;

public class PostProcessBlur extends PostProcessEffect {
    public PostProcessBlur() {
        super(ShaderHelper.INSTANCE::getBlurShader, Wrapper.INSTANCE.getMinecraft().getFramebuffer(), new SimpleFramebuffer(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight(), false, false));
    }

    @Override
    public void render() {
        checkResize();
        RenderSystem.enableTexture();
        RenderSystem.resetTextureMatrix();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        this.getFirst().endWrite();
        this.getFirst().beginRead();
        float f = this.getFirst().textureWidth;
        float g = this.getFirst().textureHeight;
        RenderSystem.viewport(0, 0, (int)f, (int)g);
        getShader().bind();
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
        getShader().detach();
        this.getSecond().endWrite();
        this.getFirst().beginWrite(true);
        this.getSecond().draw(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight(), false);
        getShader().setUpdateUniforms(null);
        this.getFirst().endWrite();
        this.getFirst().beginRead();
        RenderSystem.viewport(0, 0, (int)f, (int)g);
        getShader().bind();
        this.getSecond().clear(MinecraftClient.IS_SYSTEM_MAC);
        this.getSecond().beginWrite(false);
        RenderSystem.depthFunc(519);
        bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        bufferBuilder.vertex(0.0, 0.0, 500.0).next();
        bufferBuilder.vertex(f, 0.0, 500.0).next();
        bufferBuilder.vertex(f, g, 500.0).next();
        bufferBuilder.vertex(0.0, g, 500.0).next();
        BufferRenderer.drawWithoutShader(bufferBuilder.end());
        RenderSystem.depthFunc(515);
        getShader().detach();
        this.getSecond().endWrite();
        this.getFirst().endRead();

        Wrapper.INSTANCE.getMinecraft().getFramebuffer().beginWrite(true);
    }
}
