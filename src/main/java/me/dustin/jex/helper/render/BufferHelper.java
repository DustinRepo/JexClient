package me.dustin.jex.helper.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.render.shader.ShaderProgram;
import net.minecraft.client.render.*;

import java.util.function.Supplier;

public enum BufferHelper {
    INSTANCE;

    public void begin(BufferBuilder bufferBuilder, VertexFormat.DrawMode drawMode, VertexFormat format) {
        bufferBuilder.begin(drawMode, format);
    }

    public void begin(VertexFormat.DrawMode drawMode, VertexFormat format) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        begin(bufferBuilder, drawMode, format);
    }

    public void drawWithShader(BufferBuilder bufferBuilder, ShaderProgram shaderProgram) {
        shaderProgram.bind();
        bufferBuilder.clear();
        BufferRenderer.drawWithoutShader(bufferBuilder.end());
        shaderProgram.detach();
    }

    public void drawWithShader(BufferBuilder bufferBuilder, Supplier<Shader> shaderProgram) {
        RenderSystem.setShader(shaderProgram);
        bufferBuilder.clear();
        BufferRenderer.drawWithShader(bufferBuilder.end());
    }
}
