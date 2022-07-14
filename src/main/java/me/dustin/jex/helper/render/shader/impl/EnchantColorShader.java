package me.dustin.jex.helper.render.shader.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.math.Matrix4x4;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.shader.ShaderProgram;
import me.dustin.jex.helper.render.shader.ShaderUniform;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vector4f;

public class EnchantColorShader extends ShaderProgram {
    private final ShaderUniform sampler, projection, modelView, textureMatrix, colorModulator, glintColor, crazyRainbow, saturation, alpha, mathmode;
    public EnchantColorShader() {
        super("rainbow_enchant");
        this.sampler = addUniform("Sampler");
        this.projection = addUniform("Projection");
        this.modelView = addUniform("ModelView");
        this.colorModulator = addUniform("ColorModulator");
        this.textureMatrix = addUniform("TextureMatrix");
        this.glintColor = addUniform("GlintColor");
        this.crazyRainbow = addUniform("CrazyRainbow");
        this.saturation = addUniform("Saturation");
        this.alpha = addUniform("Alpha");
        this.mathmode = addUniform("MathMode");
        this.bindAttribute("Position", 0);
        this.bindAttribute("Tex", 1);
    }

    @Override
    public void updateUniforms() {
        float[] c = RenderSystem.getShaderColor();
        sampler.setInt(0);
        projection.setMatrix(Matrix4x4.copyFromColumnMajor(RenderSystem.getProjectionMatrix()));
        modelView.setMatrix(Matrix4x4.copyFromColumnMajor(RenderSystem.getModelViewMatrix()));
        textureMatrix.setMatrix(Matrix4x4.copyFromColumnMajor(RenderSystem.getTextureMatrix()));
        colorModulator.setVec(new Vector4f(c[0], c[1], c[2], c[3]));
        glintColor.setVec(new Vector4f(1, 1, 1, 1));
        crazyRainbow.setBoolean(true);
        saturation.setFloat(1);
        alpha.setFloat(1);
        mathmode.setInt(0);
    }
}