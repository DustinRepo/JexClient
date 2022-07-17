package me.dustin.jex.helper.render.shader.post;

import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.shader.ShaderProgram;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;

import java.util.function.Supplier;

public abstract class PostProcessEffect {
    private final Supplier<ShaderProgram> shaderProgramSupplier;
    private Framebuffer first;
    private Framebuffer second;

    private int lastWidth, lastHeight;

    public PostProcessEffect(Supplier<ShaderProgram> shaderProgramSupplier, Framebuffer first, Framebuffer second) {
        this.shaderProgramSupplier = shaderProgramSupplier;
        this.first = first;
        this.second = second;
    }

    public PostProcessEffect(Supplier<ShaderProgram> shaderProgramSupplier) {
        this.shaderProgramSupplier = shaderProgramSupplier;
        this.first = new SimpleFramebuffer(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight(), false, false);
        this.second = new SimpleFramebuffer(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight(), false, false);
    }

    public abstract void render();

    protected void checkResize() {
        if (lastHeight != Wrapper.INSTANCE.getWindow().getFramebufferHeight() || lastWidth != Wrapper.INSTANCE.getWindow().getFramebufferWidth()) {
            first.resize(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight(), false);
            second.resize(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight(), false);
        }
        lastWidth = Wrapper.INSTANCE.getWindow().getFramebufferWidth();
        lastHeight = Wrapper.INSTANCE.getWindow().getFramebufferHeight();
    }

    public ShaderProgram getShader() {
        return shaderProgramSupplier.get();
    }

    public Framebuffer getFirst() {
        return first;
    }

    public void setFirst(Framebuffer first) {
        this.first = first;
    }

    public Framebuffer getSecond() {
        return second;
    }

    public void setSecond(Framebuffer second) {
        this.second = second;
    }
}
