package me.dustin.jex.helper.render.shader.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.math.Matrix4x4;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.shader.ShaderProgram;
import me.dustin.jex.helper.render.shader.ShaderUniform;
import net.minecraft.util.math.Vec2f;

public class BlurShader extends ShaderProgram {
    private final ShaderUniform sampler, projection, size, radius, blurDir;
    public BlurShader() {
        super("blur");
        this.sampler = addUniform("Sampler");
        this.projection = addUniform("Projection");
        this.size = addUniform("Size");
        this.radius = addUniform("Radius");
        this.blurDir = addUniform("BlurDir");
        this.bindAttribute("Position", 0);
    }

    @Override
    public void updateUniforms() {
        size.setVec(new Vec2f(Wrapper.INSTANCE.getMinecraft().getFramebuffer().viewportWidth, Wrapper.INSTANCE.getMinecraft().getFramebuffer().viewportHeight));
        sampler.setInt(0);
        radius.setFloat(5);
        blurDir.setVec(new Vec2f(1, 0));
        projection.setMatrix(Matrix4x4.copyFromColumnMajor(RenderSystem.getProjectionMatrix()));
    }
}