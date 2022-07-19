package me.dustin.jex.helper.render.shader.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.render.shader.ShaderProgram;
import me.dustin.jex.helper.render.shader.ShaderUniform;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vector4f;

public class OpacityXrayShader extends ShaderProgram {
    private final ShaderUniform sampler, sampler2, modelView, projection, chunkOffset, colorModulator, fogStart, fogEnd, fogColor, fogShape, alpha;
    public OpacityXrayShader() {
        super("opacity_xray");
        this.sampler = addUniform("Sampler");
        this.sampler2 = addUniform("Sampler2");
        this.modelView = addUniform("ModelView");
        this.projection = addUniform("Projection");
        this.chunkOffset = addUniform("ChunkOffset");
        this.colorModulator = addUniform("ColorModulator");
        this.fogStart = addUniform("FogStart");
        this.fogEnd = addUniform("FogEnd");
        this.fogColor = addUniform("FogColor");
        this.fogShape = addUniform("FogShape");
        this.alpha = addUniform("Alpha");
        this.bindAttribute("Position", 0);
        this.bindAttribute("Color", 1);
        this.bindAttribute("Tex", 2);
        this.bindAttribute("Tex2", 3);
        this.bindAttribute("Normal", 4);
    }

    @Override
    public void updateUniforms() {
        float[] colorModulator = RenderSystem.getShaderColor();
        float[] fog = RenderSystem.getShaderFogColor();
        this.sampler.setInt(FabricLoaderImpl.INSTANCE.isModLoaded("sodium") ? 2 : 0);
        this.sampler2.setInt(FabricLoaderImpl.INSTANCE.isModLoaded("sodium") ? 3 : 1);
        this.projection.setMatrix(RenderSystem.getProjectionMatrix());
        this.modelView.setMatrix(RenderSystem.getModelViewMatrix());
        this.chunkOffset.setVec(new Vec3d(0, 0, 0));
        this.colorModulator.setVec(new Vector4f(colorModulator[0], colorModulator[1], colorModulator[2], colorModulator[3]));
        this.fogStart.setFloat(RenderSystem.getShaderFogStart());
        this.fogEnd.setFloat(RenderSystem.getShaderFogEnd());
        this.fogColor.setVec(new Vector4f(fog[0], fog[1], fog[2], fog[3]));
        this.fogShape.setInt(RenderSystem.getShaderFogShape().getId());
        this.alpha.setFloat(0.5f);
    }
}
