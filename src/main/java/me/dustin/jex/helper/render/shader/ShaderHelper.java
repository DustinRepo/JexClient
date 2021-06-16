package me.dustin.jex.helper.render.shader;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.util.Identifier;

import java.io.IOException;

public class ShaderHelper {

    public static Framebuffer fbo;
    public static ShaderEffect shaderEffect;
    public static Identifier identifier_1 = new Identifier("jex", "shaders/entity_outline.json");

    public static void drawFBO() {
        if (canDrawFBO()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
            fbo.draw(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight(), false);
            RenderSystem.disableBlend();
        }
    }

    public static void onResized(int int_1, int int_2) {
        if (shaderEffect != null) {
            shaderEffect.setupDimensions(int_1, int_2);
        }
    }
    public static boolean canDrawFBO() {
        return fbo != null && shaderEffect != null && Wrapper.INSTANCE.getLocalPlayer() != null;
    }

    public static void load()
    {
        if (shaderEffect != null) {
            close();
        }

        try {
            shaderEffect = new ShaderEffect(Wrapper.INSTANCE.getMinecraft().getTextureManager(), Wrapper.INSTANCE.getMinecraft().getResourceManager(), Wrapper.INSTANCE.getMinecraft().getFramebuffer(), identifier_1);
            shaderEffect.setupDimensions(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight());
            fbo = shaderEffect.getSecondaryTarget("final");
        } catch (IOException | JsonSyntaxException var3) {
            var3.printStackTrace();
            shaderEffect = null;
            fbo = null;
        }

    }

    public static void close() {
        if (shaderEffect != null) {
            shaderEffect.close();
        }

    }

}
