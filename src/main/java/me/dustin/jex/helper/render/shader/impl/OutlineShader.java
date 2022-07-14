package me.dustin.jex.helper.render.shader.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.math.Matrix4x4;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.shader.ShaderProgram;
import me.dustin.jex.helper.render.shader.ShaderUniform;
import net.minecraft.util.math.Vec2f;

public class OutlineShader extends ShaderProgram {
    private final ShaderUniform sampler, width, size, glow, glowIntensity, projection;
    public OutlineShader() {
        super("outline");
        this.sampler = addUniform("Sampler");
        this.width = addUniform("Width");
        this.size = addUniform("Size");
        this.glow = addUniform("Glow");
        this.glowIntensity = addUniform("GlowIntensity");
        this.projection = addUniform("Projection");
        this.bindAttribute("Position", 0);
    }

    @Override
    public void updateUniforms() {
        width.setInt(1);
        size.setVec(new Vec2f(Wrapper.INSTANCE.getMinecraft().getFramebuffer().viewportWidth, Wrapper.INSTANCE.getMinecraft().getFramebuffer().viewportHeight));
        sampler.setInt(0);
        glowIntensity.setFloat(1);
        glow.setBoolean(true);
        projection.setMatrix(Matrix4x4.copyFromColumnMajor(RenderSystem.getProjectionMatrix()));
    }
}