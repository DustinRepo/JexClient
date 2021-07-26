package me.dustin.jex.helper.render.shader.impl;

import me.dustin.jex.helper.render.shader.ShaderProgram;
import me.dustin.jex.helper.render.shader.ShaderUniform;

public class PosTexShader extends ShaderProgram {

    private ShaderUniform projection, modelView, sampler;

    public PosTexShader() {
        super("posTex");
        projection = addUniform("Projection");
        modelView = addUniform("ModelView");
        sampler = addUniform("Sampler");
        this.bindAttribute("Position", 0);
        this.bindAttribute("Tex", 1);
    }

    @Override
    public void updateUniforms() {
        sampler.setInt(0);
    }
}
