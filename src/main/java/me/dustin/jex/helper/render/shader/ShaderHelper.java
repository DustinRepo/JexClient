package me.dustin.jex.helper.render.shader;

import me.dustin.jex.helper.render.shader.impl.BlurShader;
import me.dustin.jex.helper.render.shader.impl.EnchantColorShader;
import me.dustin.jex.helper.render.shader.impl.OutlineShader;

public enum ShaderHelper {
    INSTANCE;
    private OutlineShader outlineShader;
    private EnchantColorShader enchantColorShader;
    private BlurShader blurShader;

    public void loadShaders() {
        outlineShader = new OutlineShader();
        enchantColorShader = new EnchantColorShader();
        blurShader = new BlurShader();
    }

    public OutlineShader getOutlineShader() {
        return outlineShader;
    }

    public EnchantColorShader getEnchantColorShader() {
        return enchantColorShader;
    }

    public BlurShader getBlurShader() {
        return blurShader;
    }
}
