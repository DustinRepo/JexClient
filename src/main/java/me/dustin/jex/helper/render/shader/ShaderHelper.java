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

    public static Framebuffer storageFBO;
    public static ShaderEffect storageShader;
    public static Framebuffer boxOutlineFBO;
    public static ShaderEffect boxOutlineShader;
    public static Identifier identifier_1 = new Identifier("jex", "shaders/entity_outline.json");

    public static void drawStorageFBO() {
        if (canDrawFBO()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
            storageFBO.draw(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight(), false);
            RenderSystem.disableBlend();
        }
    }

    public static void drawBoxOutlineFBO() {
        if (canDrawFBO()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
            boxOutlineFBO.draw(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight(), false);
            RenderSystem.disableBlend();
        }
    }

    public static void onResized(int int_1, int int_2) {
        if (storageShader != null) {
            storageShader.setupDimensions(int_1, int_2);
        }
        if (boxOutlineShader != null) {
            boxOutlineShader.setupDimensions(int_1, int_2);
        }
    }
    public static boolean canDrawFBO() {
        return storageFBO != null && storageShader != null && Wrapper.INSTANCE.getLocalPlayer() != null;
    }

    public static void load()
    {
        if (storageShader != null) {
            storageShader.close();
        }
        if (boxOutlineShader != null) {
            boxOutlineShader.close();
        }

        try {
            storageShader = new ShaderEffect(Wrapper.INSTANCE.getMinecraft().getTextureManager(), Wrapper.INSTANCE.getMinecraft().getResourceManager(), Wrapper.INSTANCE.getMinecraft().getFramebuffer(), identifier_1);
            storageShader.setupDimensions(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight());
            storageFBO = storageShader.getSecondaryTarget("final");
            boxOutlineShader = new ShaderEffect(Wrapper.INSTANCE.getMinecraft().getTextureManager(), Wrapper.INSTANCE.getMinecraft().getResourceManager(), Wrapper.INSTANCE.getMinecraft().getFramebuffer(), identifier_1);
            boxOutlineShader.setupDimensions(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight());
            boxOutlineFBO = boxOutlineShader.getSecondaryTarget("final");
        } catch (IOException | JsonSyntaxException var3) {
            storageShader = null;
            storageFBO = null;
        }

    }

    public static void close() {
        if (storageShader != null) {
            storageShader.close();
        }
        if (boxOutlineShader != null) {
            boxOutlineShader.close();
        }
    }

}
