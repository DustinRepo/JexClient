package me.dustin.jex.helper.render.shader.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.math.Matrix4x4;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.shader.ShaderProgram;
import me.dustin.jex.helper.render.shader.ShaderUniform;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.util.math.Matrix4f;
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
        this.size.setVec(new Vec2f(Wrapper.INSTANCE.getMinecraft().getFramebuffer().viewportWidth, Wrapper.INSTANCE.getMinecraft().getFramebuffer().viewportHeight));
        this.sampler.setInt(FabricLoaderImpl.INSTANCE.isModLoaded("sodium") ? 2 : 0);
        this.blurDir.setVec(new Vec2f(1, 0));
        this.projection.setMatrix(Matrix4f.projectionMatrix(0.0f, Wrapper.INSTANCE.getMinecraft().getFramebuffer().textureWidth, Wrapper.INSTANCE.getMinecraft().getFramebuffer().textureHeight, 0.0f, 0.1f, 1000.0f));
    }
}