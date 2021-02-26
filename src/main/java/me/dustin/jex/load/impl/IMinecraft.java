package me.dustin.jex.load.impl;

import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Session;

public interface IMinecraft {

    void setSession(Session session);

    void setRightClickDelayTimer(int timer);

    int getFPS();
    BufferBuilderStorage getBufferBuilderStorage();

    RenderTickCounter getRenderTickCounter();

}
