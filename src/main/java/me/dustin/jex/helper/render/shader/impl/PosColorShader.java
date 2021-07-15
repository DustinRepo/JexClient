package me.dustin.jex.helper.render.shader.impl;

import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.helper.render.shader.ShaderProgram;
import me.dustin.jex.helper.render.shader.ShaderUniform;

public class PosColorShader extends ShaderProgram {

    private ShaderUniform projection, modelView;

    public PosColorShader() {
        super("posColor");
        projection = addUniform("Projection");
        modelView = addUniform("ModelView");
        this.bindAttribute("Position", 0);
        this.bindAttribute("Color", 1);
    }

    @Override
    public void updateUniforms() {
        projection.setMatrix(ShaderHelper.INSTANCE.getProjectionMatrix());//should be RenderSystem.getProjectionMatrix() but fuck me I guess
        modelView.setMatrix(ShaderHelper.INSTANCE.getModelViewMatrix());
    }
}
