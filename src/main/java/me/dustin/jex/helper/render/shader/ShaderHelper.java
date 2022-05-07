package me.dustin.jex.helper.render.shader;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

public enum ShaderHelper {
    INSTANCE;
    public RenderTarget storageFBO;
    public PostChain storageShader;
    public RenderTarget boxOutlineFBO;
    public PostChain boxOutlineShader;
    public final ResourceLocation identifier_1 = new ResourceLocation("jex", "shaders/entity_outline.json");

    private static ShaderInstance rainbowEnchantShader;
    private static ShaderInstance translucentShader;
    private static ShaderInstance testShader;

    public void drawStorageFBO() {
        if (canDrawFBO()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            storageFBO.blitToScreen(Wrapper.INSTANCE.getWindow().getWidth(), Wrapper.INSTANCE.getWindow().getHeight(), false);
            RenderSystem.disableBlend();
        }
    }

    public void drawBoxOutlineFBO() {
        if (canDrawFBO()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            boxOutlineFBO.blitToScreen(Wrapper.INSTANCE.getWindow().getWidth(), Wrapper.INSTANCE.getWindow().getHeight(), false);
            RenderSystem.disableBlend();
        }
    }

    public void onResized(int int_1, int int_2) {
        if (storageShader != null) {
            storageShader.resize(int_1, int_2);
        }
        if (boxOutlineShader != null) {
            boxOutlineShader.resize(int_1, int_2);
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
            storageShader = new PostChain(Wrapper.INSTANCE.getMinecraft().getTextureManager(), Wrapper.INSTANCE.getMinecraft().getResourceManager(), Wrapper.INSTANCE.getMinecraft().getMainRenderTarget(), identifier_1);
            storageShader.resize(Wrapper.INSTANCE.getWindow().getWidth(), Wrapper.INSTANCE.getWindow().getHeight());
            storageFBO = storageShader.getTempTarget("final");
            boxOutlineShader = new PostChain(Wrapper.INSTANCE.getMinecraft().getTextureManager(), Wrapper.INSTANCE.getMinecraft().getResourceManager(), Wrapper.INSTANCE.getMinecraft().getMainRenderTarget(), identifier_1);
            boxOutlineShader.resize(Wrapper.INSTANCE.getWindow().getWidth(), Wrapper.INSTANCE.getWindow().getHeight());
            boxOutlineFBO = boxOutlineShader.getTempTarget("final");
        } catch (Exception var3) {
            storageShader = null;
            storageFBO = null;
        }

    }

    public static void loadCustomMCShaders(ResourceProvider factory) {
        try {
            rainbowEnchantShader = new ShaderInstance(factory, "jex:rainbow_enchant", DefaultVertexFormat.POSITION_TEX);
            translucentShader = new ShaderInstance(factory, "jex:translucent", DefaultVertexFormat.BLOCK);
            testShader = new ShaderInstance(factory, "jex:test", DefaultVertexFormat.POSITION_COLOR);
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

    public static ShaderInstance getRainbowEnchantShader() {
        return rainbowEnchantShader;
    }

    public static ShaderInstance getTranslucentShader() {
        return translucentShader;
    }

    public static ShaderInstance getTestShader() {
        return testShader;
    }

}
