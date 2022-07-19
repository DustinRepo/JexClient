package me.dustin.jex.helper.render.shader;

import me.dustin.jex.helper.render.shader.impl.*;

public enum ShaderHelper {
    INSTANCE;
    private PosColorShader posColorShader;
    private OutlineShader outlineShader;
    private EnchantColorShader enchantColorShader;
    private BlurShader blurShader;
    private OpacityXrayShader opacityXrayShader;

    public void loadShaders() {
        posColorShader = new PosColorShader();
        outlineShader = new OutlineShader();
        enchantColorShader = new EnchantColorShader();
        blurShader = new BlurShader();
        opacityXrayShader = new OpacityXrayShader();
    }

    public PosColorShader getPosColorShader() {
        return posColorShader;
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

    public OpacityXrayShader getOpacityXrayShader() {
        return opacityXrayShader;
    }
}
