package me.dustin.jex.helper.render.shader;

import me.dustin.jex.helper.render.shader.impl.OutlineShader;
import net.minecraft.client.render.*;
import net.minecraft.resource.ResourceFactory;

import java.io.IOException;

public enum ShaderHelper {
    INSTANCE;
    private static Shader rainbowEnchantShader;

    private static OutlineShader outlineShader;

    public static void loadShaders(ResourceFactory factory) throws IOException {
        outlineShader = new OutlineShader();
        if (rainbowEnchantShader == null)
            rainbowEnchantShader = new Shader(factory, "jex:rainbow_enchant", VertexFormats.POSITION_TEXTURE);
    }

    public static Shader getRainbowEnchantShader() {
        return rainbowEnchantShader;
    }

    public OutlineShader getOutlineShader() {
        return outlineShader;
    }
}
