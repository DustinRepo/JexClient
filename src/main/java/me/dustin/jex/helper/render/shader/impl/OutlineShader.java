package me.dustin.jex.helper.render.shader.impl;

import me.dustin.jex.helper.math.vector.Vector2D;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.shader.ShaderProgram;
import me.dustin.jex.helper.render.shader.ShaderUniform;

public class OutlineShader extends ShaderProgram {
    private ShaderUniform sampler, texel, width;
    public OutlineShader() {
        super("outline");
        this.sampler = addUniform("Sampler");
        this.texel = addUniform("TexelSize");
        this.width = addUniform("Width");
        this.bindAttribute("Position", 0);
        this.bindAttribute("Position", 1);
        this.bindAttribute("Tex", 2);
    }

    @Override
    public void updateUniforms() {
        sampler.setInt(0);
        width.setInt(5);
        texel.setVec(new Vector2D(1f / Wrapper.INSTANCE.getWindow().getWidth(), 1f / Wrapper.INSTANCE.getWindow().getHeight()));
    }
}
