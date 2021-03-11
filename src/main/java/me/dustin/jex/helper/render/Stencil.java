package me.dustin.jex.helper.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IFrameBuffer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.EXTPackedDepthStencil;
import org.lwjgl.opengl.GL11;

public enum Stencil {
    INSTANCE;

    public void write() {
        checkSetupFBO();
        RenderSystem.clearStencil(0);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFFFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GlStateManager.colorMask(false, false, false, false);
    }

    public void erase() {
        RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 1, 0xFFFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GlStateManager.colorMask(true, true, true, true);
    }

    public void dispose() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    public void checkSetupFBO() {
        Framebuffer fbo = MinecraftClient.getInstance().getFramebuffer();
        if (fbo != null) {
            if (fbo.getDepthAttachment() > -1) {
                setupFBO(fbo);
                IFrameBuffer ifbo = (IFrameBuffer) fbo;
                ifbo.setDepthAttachment(-1);
            }
        }
    }

    public void setupFBO(Framebuffer fbo) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.getDepthAttachment());
        int stencil_depth_buffer_ID = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID);
        EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT, Wrapper.INSTANCE.getWindow().getWidth(), Wrapper.INSTANCE.getWindow().getHeight());
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID);
    }
}
