package me.dustin.jex.helper.render.shader.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.math.Matrix4x4;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.shader.ShaderProgram;
import me.dustin.jex.helper.render.shader.ShaderUniform;

public class PosColorTexShader extends ShaderProgram {

    private ShaderUniform projection, modelView, sampler;

    public PosColorTexShader() {
        super("posColorTex");
        projection = addUniform("Projection");
        modelView = addUniform("ModelView");
        sampler = addUniform("Sampler");
        this.bindAttribute("Position", 0);
        this.bindAttribute("Color", 1);
        this.bindAttribute("Tex", 2);
    }

    @Override
    public void updateUniforms() {
        Matrix4x4 ortho = Matrix4x4.ortho2DMatrix(0, Render2DHelper.INSTANCE.getScaledWidth(), Render2DHelper.INSTANCE.getScaledHeight(), 0, -0.1f, 1000.f);
        projection.setMatrix(ortho);//should be RenderSystem.getProjectionMatrix() but fuck me I guess
        modelView.setMatrix(RenderSystem.getModelViewMatrix());
        sampler.setInt(0);
    }
}
