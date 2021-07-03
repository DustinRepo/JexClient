package me.dustin.jex.helper.render.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public enum ShaderHelper {
    INSTANCE;
    public Framebuffer storageFBO;
    public ShaderEffect storageShader;
    public Framebuffer boxOutlineFBO;
    public ShaderEffect boxOutlineShader;
    public Identifier identifier_1 = new Identifier("jex", "shaders/entity_outline.json");

    private static Shader rainbowEnchantShader;
    private static Shader translucentShader;
    private static Shader testShader;

    public void drawStorageFBO() {
        if (canDrawFBO()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
            storageFBO.draw(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight(), false);
            RenderSystem.disableBlend();
        }
    }

    public void drawBoxOutlineFBO() {
        if (canDrawFBO()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
            boxOutlineFBO.draw(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight(), false);
            RenderSystem.disableBlend();
        }
    }

    public void onResized(int int_1, int int_2) {
        if (storageShader != null) {
            storageShader.setupDimensions(int_1, int_2);
        }
        if (boxOutlineShader != null) {
            boxOutlineShader.setupDimensions(int_1, int_2);
        }
    }
    public boolean canDrawFBO() {
        return storageFBO != null && storageShader != null && Wrapper.INSTANCE.getLocalPlayer() != null;
    }

    public void load()
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
        } catch (Exception var3) {
            storageShader = null;
            storageFBO = null;
        }

    }

    public static void loadCustomMCShaders() {
        try {
            rainbowEnchantShader = new Shader(Wrapper.INSTANCE.getMinecraft().getResourcePackProvider().getPack(), "jex:rainbow_enchant", VertexFormats.POSITION_TEXTURE);
            translucentShader = new Shader(Wrapper.INSTANCE.getMinecraft().getResourcePackProvider().getPack(), "jex:translucent", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
            testShader = new Shader(Wrapper.INSTANCE.getMinecraft().getResourcePackProvider().getPack(), "jex:test", VertexFormats.POSITION_COLOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (storageShader != null) {
            storageShader.close();
        }
        if (boxOutlineShader != null) {
            boxOutlineShader.close();
        }
    }

    public static Shader getRainbowEnchantShader() {
        return rainbowEnchantShader;
    }

    public static Shader getTranslucentShader() {
        return translucentShader;
    }

    public static Shader getTestShader() {
        return testShader;
    }

}
