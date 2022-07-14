package me.dustin.jex.load.impl;

import net.minecraft.client.gl.Framebuffer;

public interface IWorldRenderer {
    Framebuffer getEntityOutlinesFramebuffer();
    void setEntityOutlinesFramebuffer(Framebuffer framebuffer);
}
