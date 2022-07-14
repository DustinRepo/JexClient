package me.dustin.jex.helper.render.shader;

import me.dustin.jex.helper.render.shader.impl.EnchantColorShader;
import me.dustin.jex.helper.render.shader.impl.OutlineShader;

public enum ShaderHelper {
    INSTANCE;
    private OutlineShader outlineShader;
    private EnchantColorShader enchantColorShader;

    public void loadShaders() {
        outlineShader = new OutlineShader();
        enchantColorShader = new EnchantColorShader();
    }

    public OutlineShader getOutlineShader() {
        return outlineShader;
    }

    public EnchantColorShader getEnchantColorShader() {
        return enchantColorShader;
    }
}
