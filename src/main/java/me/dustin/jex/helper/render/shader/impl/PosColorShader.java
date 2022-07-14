package me.dustin.jex.helper.render.shader.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.math.Matrix4x4;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.shader.ShaderProgram;
import me.dustin.jex.helper.render.shader.ShaderUniform;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec2f;

public class PosColorShader extends ShaderProgram {
    private final ShaderUniform projection, modelView;
    public PosColorShader() {
        super("posColor");
        this.projection = addUniform("Projection");
        this.modelView = addUniform("ModelView");
        this.bindAttribute("Position", 0);
        this.bindAttribute("Color", 1);
    }

    @Override
    public void updateUniforms() {
        this.projection.setMatrix(Matrix4x4.copyFromColumnMajor(RenderSystem.getProjectionMatrix()));
        this.modelView.setMatrix(Matrix4x4.copyFromColumnMajor(RenderSystem.getModelViewMatrix()));
    }
}