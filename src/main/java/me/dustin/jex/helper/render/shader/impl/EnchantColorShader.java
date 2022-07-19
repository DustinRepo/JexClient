package me.dustin.jex.helper.render.shader.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.math.Matrix4x4;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.shader.ShaderProgram;
import me.dustin.jex.helper.render.shader.ShaderUniform;
import net.fabricmc.loader.impl.FabricLoaderImpl;
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
        this.sampler.setInt(FabricLoaderImpl.INSTANCE.isModLoaded("sodium") ? 2 : 0);
        this.projection.setMatrix(RenderSystem.getProjectionMatrix());
        this.modelView.setMatrix(RenderSystem.getModelViewMatrix());
        this.textureMatrix.setMatrix(RenderSystem.getTextureMatrix());
        this.colorModulator.setVec(new Vector4f(c[0], c[1], c[2], c[3]));
        this.glintColor.setVec(new Vector4f(1, 1, 1, 1));
        this.crazyRainbow.setBoolean(true);
        this.saturation.setFloat(1);
        this.alpha.setFloat(1);
        this.mathmode.setInt(0);
    }
}